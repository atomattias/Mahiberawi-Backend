# Environment Setup Guide

This guide explains how to set up separate development and production environments for the Mahiberawi Backend.

## 🏗️ Architecture Overview

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Development   │    │    Staging      │    │   Production    │
│   Environment   │    │   Environment   │    │   Environment   │
├─────────────────┤    ├─────────────────┤    ├─────────────────┤
│ Railway Project │    │ Railway Project │    │ Railway Project │
│ Mahiberawi-Dev  │    │Mahiberawi-Stage │    │ Mahiberawi-Prod │
├─────────────────┤    ├─────────────────┤    ├─────────────────┤
│ Branch: dev     │    │ Branch: staging │    │ Branch: prod    │
│ Database: Dev   │    │ Database: Stage │    │ Database: Prod  │
│ Config: dev     │    │ Config: staging │    │ Config: prod    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 📋 Prerequisites

- Railway account
- GitHub repository access
- Separate databases for each environment

## 🚀 Setup Steps

### 1. Create Development Railway Project

1. **Go to Railway Dashboard**
2. **Click "New Project"**
3. **Choose "Deploy from GitHub repo"**
4. **Select**: `Mahiberawi-Backend`
5. **Set Branch**: `development`
6. **Name Project**: `Mahiberawi-Backend-Dev`

### 2. Create Staging Railway Project

1. **Go to Railway Dashboard**
2. **Click "New Project"**
3. **Choose "Deploy from GitHub repo"**
4. **Select**: `Mahiberawi-Backend`
5. **Set Branch**: `staging`
6. **Name Project**: `Mahiberawi-Backend-Staging`

### 3. Set Up Development Environment Variables

In your **Development Railway Project**, add these variables:

```bash
# Database
DATABASE_URL_DEV=postgresql://username:password@host:port/database
DB_USERNAME_DEV=your_dev_username
DB_PASSWORD_DEV=your_dev_password

# JWT Secrets (different from production)
JWT_SECRET_DEV=your-dev-jwt-secret-key
JWT_REFRESH_SECRET_DEV=your-dev-refresh-secret-key

# Email Configuration
MAIL_USERNAME_DEV=your-dev-email@gmail.com
MAIL_PASSWORD_DEV=your-dev-app-password

# Twilio (Development)
TWILIO_ACCOUNT_SID_DEV=your-dev-twilio-sid
TWILIO_AUTH_TOKEN_DEV=your-dev-twilio-token
TWILIO_PHONE_NUMBER_DEV=+1234567890

# TeleBirr (Development)
TELEBIRR_APP_KEY_DEV=your-dev-telebirr-key
TELEBIRR_APP_SECRET_DEV=your-dev-telebirr-secret

# Admin Promotion
ADMIN_PROMOTION_KEY_DEV=dev-promotion-key

# Spring Profile
SPRING_PROFILES_ACTIVE=development
```

### 4. Set Up Staging Environment Variables

In your **Staging Railway Project**, add these variables:

```bash
# Database
DATABASE_URL_STAGING=postgresql://username:password@host:port/database
DB_USERNAME_STAGING=your_staging_username
DB_PASSWORD_STAGING=your_staging_password

# JWT Secrets (different from dev and prod)
JWT_SECRET_STAGING=your-staging-jwt-secret-key
JWT_REFRESH_SECRET_STAGING=your-staging-refresh-secret-key

# Email Configuration
MAIL_USERNAME_STAGING=your-staging-email@gmail.com
MAIL_PASSWORD_STAGING=your-staging-app-password

# Twilio (Staging)
TWILIO_ACCOUNT_SID_STAGING=your-staging-twilio-sid
TWILIO_AUTH_TOKEN_STAGING=your-staging-twilio-token
TWILIO_PHONE_NUMBER_STAGING=+1234567890

# TeleBirr (Staging)
TELEBIRR_APP_KEY_STAGING=your-staging-telebirr-key
TELEBIRR_APP_SECRET_STAGING=your-staging-telebirr-secret

# Admin Promotion
ADMIN_PROMOTION_KEY_STAGING=staging-promotion-key

# Spring Profile
SPRING_PROFILES_ACTIVE=staging
```

### 5. Update Production Environment Variables

In your **Production Railway Project**, ensure these are set:

```bash
# Database
DATABASE_URL=postgresql://username:password@host:port/database
DB_USERNAME=your_prod_username
DB_PASSWORD=your_prod_password

# JWT Secrets (production secrets)
JWT_SECRET=your-production-jwt-secret-key
JWT_REFRESH_SECRET=your-production-refresh-secret-key

# Email Configuration
MAIL_USERNAME=your-prod-email@gmail.com
MAIL_PASSWORD=your-prod-app-password

# Twilio (Production)
TWILIO_ACCOUNT_SID=your-prod-twilio-sid
TWILIO_AUTH_TOKEN=your-prod-twilio-token
TWILIO_PHONE_NUMBER=+1234567890

# TeleBirr (Production)
TELEBIRR_APP_KEY=your-prod-telebirr-key
TELEBIRR_APP_SECRET=your-prod-telebirr-secret

# Admin Promotion
ADMIN_PROMOTION_KEY=prod-promotion-key

# Spring Profile
SPRING_PROFILES_ACTIVE=production
```

## 🔧 Configuration Files

### Development Configuration
- **File**: `src/main/resources/application-development.properties`
- **Profile**: `development`
- **Features**: Debug logging, H2 console, test data enabled

### Staging Configuration
- **File**: `src/main/resources/application-staging.properties`
- **Profile**: `staging`
- **Features**: Production-like settings, testing environment

### Production Configuration
- **File**: `src/main/resources/application-production.properties`
- **Profile**: `production`
- **Features**: Optimized logging, security hardened

## 📜 Available Scripts

### Promotion Scripts
```bash
# Development environment
./promote-user-to-super-admin-dev.sh
./promote-user-to-super-admin-dev.ps1

# Staging environment
./promote-user-to-super-admin-staging.sh
./promote-user-to-super-admin-staging.ps1

# Production environment
./promote-user-to-super-admin.sh
./promote-user-to-super-admin.ps1
```

### Deployment Scripts
```bash
# Deploy to development
./deploy-to-development.sh

# Deploy to staging
./deploy-to-staging.sh

# Deploy to production
./deploy-to-production.sh
```

## 🔄 Workflow

### Development Workflow
1. **Work on**: `development` branch
2. **Test changes** locally
3. **Deploy**: `./deploy-to-development.sh`
4. **Test in dev environment**
5. **Merge to staging** when ready

### Staging Workflow
1. **Merge from**: `development` → `staging`
2. **Deploy**: `./deploy-to-staging.sh`
3. **Test thoroughly** in staging environment
4. **Verify all features** work correctly
5. **Merge to production** when approved

### Production Workflow
1. **Merge from**: `staging` → `production`
2. **Review changes**
3. **Deploy**: `./deploy-to-production.sh`
4. **Monitor deployment**

## 🔍 Environment URLs

### Development
- **URL**: `https://mahiberawi-backend-dev.up.railway.app`
- **Health Check**: `/api/health`
- **H2 Console**: `/api/h2-console` (if enabled)

### Staging
- **URL**: `https://mahiberawi-backend-staging.up.railway.app`
- **Health Check**: `/api/health`

### Production
- **URL**: `https://mahiberawi-backend-production.up.railway.app`
- **Health Check**: `/api/health`

## 🛡️ Security Considerations

### Development Environment
- ✅ Debug logging enabled
- ✅ H2 console accessible
- ✅ Test data allowed
- ✅ Shorter verification codes
- ⚠️ Less secure defaults

### Staging Environment
- ✅ Production-like settings
- ✅ Debug logging disabled
- ✅ H2 console disabled
- ✅ Strict validation
- ✅ Testing environment
- ✅ Separate database

### Production Environment
- ✅ Debug logging disabled
- ✅ H2 console disabled
- ✅ Strict validation
- ✅ Secure secrets required
- ✅ Optimized performance

## 🚨 Important Notes

1. **Never share production secrets** with development
2. **Use different databases** for each environment
3. **Test thoroughly** in development before production
4. **Monitor deployments** for any issues
5. **Keep environment variables** secure and separate

## 🔧 Troubleshooting

### Common Issues

1. **Environment variables not loading**
   - Check Railway project settings
   - Verify variable names match configuration files

2. **Database connection issues**
   - Ensure separate databases for each environment
   - Check connection strings

3. **Deployment failures**
   - Check Railway logs
   - Verify branch connections
   - Ensure all required variables are set

### Getting Help

- Check Railway deployment logs
- Verify environment variable configuration
- Test locally with environment profiles
- Review application logs for specific errors
