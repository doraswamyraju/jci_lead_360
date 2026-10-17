# JCI LEAD360 - Complete Hostinger VPS Deployment Manual

> **Domain**: `lead.sriddha.com`  
> **VPS IP**: `147.93.107.21`  
> **Dedicated Port**: `5087`  
> **Repository**: `https://github.com/doraswamyraju/jci_lead_360.git`  
> **PM2 Process Name**: `jci-lead-360`

---

## ⚡ Quick 1-Click Update (For Everyday Use)

Whenever you push new code and just want to update the live site on your VPS, run this single command in your SSH terminal:

```bash
cd /var/www/lead360 && git pull origin main && npm install --production && pm2 reload jci-lead-360
```

---

## 🚀 Part 1: First-Time Setup on Hostinger VPS (Run Once)

Copy and run these exact commands block by block in your SSH terminal (`root@147.93.107.21`):

### Step 1: Clone the Repository
```bash
# Ensure /var/www exists and navigate there
mkdir -p /var/www
cd /var/www

# Clone the repository into folder 'lead360'
git clone https://github.com/doraswamyraju/jci_lead_360.git lead360

# Enter the project directory
cd /var/www/lead360
```

### Step 2: Install Dependencies & Setup Environment
```bash
# Install node packages
npm install --production

# Create upload directory with permissions
mkdir -p public/uploads
chmod -R 755 public/uploads

# Create the production .env file
cat << 'EOF' > .env
PORT=5087
NODE_ENV=production
JWT_SECRET=jci_lead360_secure_jwt_token_key_2027
HOST_DOMAIN=https://lead.sriddha.com
EOF
```

### Step 3: Start Application with PM2
```bash
# Start the app on port 5087 isolated from all other sites
pm2 start src/server.js --name "jci-lead-360"

# Save PM2 process list so it automatically restarts on server reboot
pm2 save
```

Check that it's running:
```bash
pm2 status
curl http://127.0.0.1:5087/api/health
```
*(You should see `{"status":"online","app":"JCI LEAD360","port":5087}`)*

---

## 🌐 Part 2: Isolated Nginx Configuration (Safeguards All Other Sites)

This configuration creates a standalone file for `lead.sriddha.com` and **does not touch or affect any of your other websites**.

### Step 1: Create Dedicated Nginx Config
```bash
cat << 'EOF' > /etc/nginx/sites-available/lead.sriddha.com
server {
    listen 80;
    server_name lead.sriddha.com;

    # Allow high-res flyer and frame uploads up to 50MB
    client_max_body_size 50M;

    location / {
        proxy_pass http://127.0.0.1:5087;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
    }
}
EOF
```

### Step 2: Enable Site and Test Nginx
```bash
# Create symbolic link to enable site
ln -s /etc/nginx/sites-available/lead.sriddha.com /etc/nginx/sites-enabled/

# Test Nginx syntax (Must report 'syntax is ok' and 'test is successful')
nginx -t

# Reload Nginx without downtime to other sites
systemctl reload nginx
```

### Step 3: Setup Free SSL (HTTPS) with Certbot
```bash
certbot --nginx -d lead.sriddha.com --non-interactive --agree-tos -m admin@sriddha.com --redirect
```

---

## 🛠️ Part 3: Troubleshooting & Monitoring Commands

| Action | Command |
| :--- | :--- |
| **Check Live Logs** | `pm2 logs jci-lead-360` |
| **Restart App** | `pm2 restart jci-lead-360` |
| **Stop App** | `pm2 stop jci-lead-360` |
| **Check Memory/CPU** | `pm2 monit` |
| **Test Port 5087 Locally** | `curl -I http://127.0.0.1:5087` |
| **Test Nginx Config** | `nginx -t` |
| **Check Port Listening** | `netstat -tlpn \| grep 5087` or `ss -tulpn \| grep 5087` |

---

## 🔑 Initial Super Admin Credentials

- **URL**: `https://lead.sriddha.com`
- **Email**: `admin@lead360.com`
- **Password**: `Admin@2027`
