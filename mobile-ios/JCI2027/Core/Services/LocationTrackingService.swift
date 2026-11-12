import Foundation
import CoreLocation
import SwiftUI

/**
 * LocationTrackingService — Manages live member location sharing and distance calculations for the Chapter Networking Map.
 */
class LocationTrackingService: NSObject, ObservableObject, CLLocationManagerDelegate {
    static let shared = LocationTrackingService()

    private let locationManager = CLLocationManager()

    @Published var currentUserLocation: CLLocationCoordinate2D? = nil
    @Published var authorizationStatus: CLAuthorizationStatus = .notDetermined
    @AppStorage("is_location_sharing_active") var isLocationSharingActive: Bool = false

    override private init() {
        super.init()
        locationManager.delegate = self
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
        authorizationStatus = locationManager.authorizationStatus
    }

    func requestPermissionAndStart() {
        if authorizationStatus == .notDetermined {
            locationManager.requestWhenInUseAuthorization()
        } else if authorizationStatus == .authorizedWhenInUse || authorizationStatus == .authorizedAlways {
            locationManager.startUpdatingLocation()
        }
    }

    func stopTracking() {
        locationManager.stopUpdatingLocation()
    }

    func toggleLocationSharing(token: String?, currentMemberId: String?, completion: ((Bool) -> Void)? = nil) {
        isLocationSharingActive.toggle()
        if isLocationSharingActive {
            requestPermissionAndStart()
            if let loc = currentUserLocation, let token = token, let memberId = currentMemberId {
                Task {
                    _ = try? await ApiService.shared.updateMemberLocation(
                        token: token,
                        memberId: memberId,
                        lat: loc.latitude,
                        lng: loc.longitude,
                        isSharing: true
                    )
                }
            }
        } else {
            stopTracking()
            if let token = token, let memberId = currentMemberId {
                Task {
                    _ = try? await ApiService.shared.updateMemberLocation(
                        token: token,
                        memberId: memberId,
                        lat: 0,
                        lng: 0,
                        isSharing: false
                    )
                }
            }
        }
        completion?(isLocationSharingActive)
    }

    // MARK: - CLLocationManagerDelegate
    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        authorizationStatus = manager.authorizationStatus
        if authorizationStatus == .authorizedWhenInUse || authorizationStatus == .authorizedAlways {
            if isLocationSharingActive {
                locationManager.startUpdatingLocation()
            }
        }
    }

    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let latest = locations.last else { return }
        DispatchQueue.main.async {
            self.currentUserLocation = latest.coordinate
        }
    }

    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        // Location update error logged silently
    }

    /**
     * Calculates user-friendly distance between current device and a member coordinate.
     */
    func distanceFormatted(from lat: Double?, lng: Double?) -> String? {
        guard let lat = lat, let lng = lng, lat != 0, lng != 0 else { return nil }
        guard let userLoc = currentUserLocation else { return nil }

        let user = CLLocation(latitude: userLoc.latitude, longitude: userLoc.longitude)
        let target = CLLocation(latitude: lat, longitude: lng)
        let distanceMeters = user.distance(from: target)

        if distanceMeters < 1000 {
            return "\(Int(distanceMeters)) m away"
        } else {
            let km = distanceMeters / 1000.0
            return String(format: "%.1f km away", km)
        }
    }
}
