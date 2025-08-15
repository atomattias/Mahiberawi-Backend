#!/bin/bash

# Database Setup and Management Script
# This script helps set up and manage the three separate databases

echo "=== Database Setup and Management ==="
echo "===================================="

# Configuration
DEV_DB_NAME="mahiberawi-dev"
STAGING_DB_NAME="mahiberawi-stage"
PROD_DB_NAME="mahiberawi-prod"

echo
echo "Available databases:"
echo "1. Development: $DEV_DB_NAME"
echo "2. Staging: $STAGING_DB_NAME"
echo "3. Production: $PROD_DB_NAME"

echo
echo "What would you like to do?"
echo "1. Check database connections"
echo "2. Reset development database"
echo "3. Backup production database"
echo "4. Restore staging from production backup"
echo "5. Show database URLs (for Railway setup)"

read -p "Enter your choice (1-5): " CHOICE

case $CHOICE in
    1)
        echo
        echo "=== Checking Database Connections ==="
        echo "Development: $DEV_DB_NAME"
        echo "Staging: $STAGING_DB_NAME"
        echo "Production: $PROD_DB_NAME"
        echo
        echo "Note: Use Railway dashboard to check actual connections"
        ;;
    2)
        echo
        echo "⚠️  WARNING: This will reset the development database"
        echo "   All test data will be lost!"
        read -p "   Are you sure? (yes/no): " CONFIRM
        if [ "$CONFIRM" = "yes" ]; then
            echo "   Resetting development database..."
            echo "   Use Railway dashboard to reset the PostgreSQL service"
        else
            echo "   Reset cancelled"
        fi
        ;;
    3)
        echo
        echo "=== Production Database Backup ==="
        echo "Use Railway dashboard to create a backup:"
        echo "1. Go to Production project"
        echo "2. Click on PostgreSQL service"
        echo "3. Go to 'Backups' tab"
        echo "4. Click 'Create Backup'"
        ;;
    4)
        echo
        echo "=== Restore Staging from Production ==="
        echo "Use Railway dashboard to restore:"
        echo "1. Go to Staging project"
        echo "2. Click on PostgreSQL service"
        echo "3. Go to 'Backups' tab"
        echo "4. Click 'Restore' and select production backup"
        ;;
    5)
        echo
        echo "=== Database URLs for Railway Setup ==="
        echo
        echo "Development Database:"
        echo "  Service Name: mahiberawi-dev-db"
        echo "  Variable: DATABASE_URL_DEV"
        echo "  Format: postgresql://username:password@host:port/$DEV_DB_NAME"
        echo
        echo "Staging Database:"
        echo "  Service Name: mahiberawi-stage-db"
        echo "  Variable: DATABASE_URL_STAGING"
        echo "  Format: postgresql://username:password@host:port/$STAGING_DB_NAME"
        echo
        echo "Production Database:"
        echo "  Service Name: mahiberawi-prod-db"
        echo "  Variable: DATABASE_URL"
        echo "  Format: postgresql://username:password@host:port/$PROD_DB_NAME"
        ;;
    *)
        echo "Invalid choice"
        exit 1
        ;;
esac

echo
echo "=== Database Management Complete ==="
