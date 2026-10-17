# JCI LEAD360 - Hostinger VPS Deployment Guide

> **Production URL**: `https://lead.sriddha.com`  
> **Server Port**: `5087` (Protected, multi-site isolated)  
> **Repository**: `https://github.com/doraswamyraju/jci_lead_360.git`

---

## 1. One-Time Setup on Hostinger VPS (SSH)

SSH into your Hostinger VPS and run:

```bash
# 1. Clone repository to your apps directory
cd /var/www  # or your preferred folder
git clone https://github.com/doraswamyraju/jci_lead_360.git lead360
cd lead360

# 2. Install dependencies
npm install --production

# 3. Create .env configuration
cat << 'EOF' > .env
PORT=5087
NODE_ENV=production
JWT_SECRET=jci_lead360_production_jwt_secret_2027
HOST_DOMAIN=https://lead.sriddha.com
EOF

# 4. Start isolated process with PM2
pm2 start src/server.js --name "jci-lead-360"
pm2 save
```

---

## 2. Nginx Configuration for `lead.sriddha.com`

Create a dedicated server block (completely isolated from your other sites):

```bash
sudo nano /etc/nginx/sites-available/lead.sriddha.com
```

Paste the following:

```nginx
server {
    server_name lead.sriddha.com;

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
        client_max_body_size 50M;
    }
}
```

Enable the site and reload Nginx:

```bash
sudo ln -s /etc/nginx/sites-available/lead.sriddha.com /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx

# Install free SSL certificate
sudo certbot --nginx -d lead.sriddha.com
```

---

## 3. Daily Update Workflow (How to deploy future updates)

Whenever updates are committed to Git:

```bash
cd /var/www/lead360
git pull origin main
npm install --production  # only if new dependencies added
pm2 reload jci-lead-360
```
