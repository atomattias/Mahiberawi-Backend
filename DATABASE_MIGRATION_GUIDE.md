# Database Migration Guide

## Overview

This application uses a robust database migration system that ensures:
- ✅ **Data Preservation**: Your data is never lost during schema changes
- ✅ **Schema Consistency**: Database always matches entity definitions
- ✅ **Development Safety**: Safe to run multiple times without side effects
- ✅ **Production Ready**: Works with both H2 and PostgreSQL

## Migration System Components

### 1. **DatabaseConfig.java**
- Handles database initialization before JPA validation
- Runs SQL scripts to create missing tables
- Validates schema consistency
- Only runs in `validate` mode

### 2. **db-migration-complete.sql**
- Comprehensive migration script with all required tables
- Idempotent (safe to run multiple times)
- Includes indexes and constraints
- Compatible with H2 and PostgreSQL

### 3. **DatabaseMigrationConfig.java**
- Handles data migrations (role updates, column additions)
- Runs after application startup
- Updates existing data safely

## Usage Modes

### **Development Mode (Recommended)**
```bash
# Use development profile
mvn spring-boot:run -Dspring.profiles.active=dev
```

**What it does:**
- Uses `validate` mode for data safety
- Automatically creates missing tables
- Preserves existing data
- Enables H2 console at `/h2-console`

### **Production Mode**
```bash
# Use production profile (or no profile)
mvn spring-boot:run
```

**What it does:**
- Uses `validate` mode
- Requires all tables to exist
- No automatic table creation
- Optimized for performance

### **Initial Setup Mode (First Time Only)**
```bash
# For completely fresh database
mvn spring-boot:run -Dspring.jpa.hibernate.ddl-auto=create
```

**What it does:**
- Creates all tables from entity definitions
- **WARNING**: This will delete existing data
- Only use for fresh installations

## Database Configuration Options

### **DDL Auto Modes:**

| Mode | Data Safety | Use Case | Description |
|------|-------------|----------|-------------|
| `validate` | ✅ Safe | Production/Dev | Validates schema, preserves data |
| `update` | ⚠️ Risky | Development | Updates schema, may cause issues |
| `create` | ❌ Unsafe | Testing | Creates fresh schema, deletes data |
| `create-drop` | ❌ Unsafe | Testing | Creates on startup, drops on shutdown |

### **Recommended Configuration:**

```properties
# For development with data preservation
spring.jpa.hibernate.ddl-auto=validate
spring.profiles.active=dev

# For production
spring.jpa.hibernate.ddl-auto=validate
```

## Adding New Tables

### **Step 1: Create Entity**
```java
@Entity
@Table(name = "new_table")
public class NewEntity {
    // Entity definition
}
```

### **Step 2: Add to Migration Script**
```sql
-- Add to db-migration-complete.sql
CREATE TABLE IF NOT EXISTS new_table (
    id VARCHAR(255) PRIMARY KEY,
    -- other columns
);
```

### **Step 3: Add to Schema Validator**
```java
// In DatabaseConfig.java
String[] requiredTables = {
    "users", "groups", "new_table" // Add your table
};
```

## Troubleshooting

### **Missing Table Error**
```
Schema-validation: missing table [table_name]
```

**Solution:**
1. Ensure table is in `db-migration-complete.sql`
2. Check if table is in `requiredTables` array
3. Restart application

### **Data Loss Prevention**
- Always use `validate` mode in production
- Test migrations on development data first
- Use `create` mode only for fresh installations

### **Migration Order Issues**
- Tables are created in order they appear in SQL script
- Foreign key constraints are commented out for H2 compatibility
- Enable foreign keys for PostgreSQL in production

## Best Practices

### **For Development:**
1. Use `dev` profile: `--spring.profiles.active=dev`
2. Use `validate` mode for data safety
3. Test schema changes before committing
4. Use H2 console to inspect database: `http://localhost:8080/api/h2-console`

### **For Production:**
1. Use `validate` mode only
2. Run migrations manually before deployment
3. Backup database before schema changes
4. Test migrations on staging environment

### **For Testing:**
1. Use `create` or `create-drop` mode
2. Use in-memory H2 database
3. Reset data between tests

## Migration Script Structure

```sql
-- 1. Table Creation (IF NOT EXISTS)
CREATE TABLE IF NOT EXISTS table_name (
    -- columns
);

-- 2. Indexes (IF NOT EXISTS)
CREATE INDEX IF NOT EXISTS idx_name ON table_name(column);

-- 3. Constraints (IF NOT EXISTS)
ALTER TABLE table_name ADD CONSTRAINT IF NOT EXISTS constraint_name UNIQUE (columns);

-- 4. Data Updates (Safe)
UPDATE table_name SET column = value WHERE condition;
```

## Environment Variables

```bash
# Database URL (for PostgreSQL)
DATABASE_URL=postgresql://user:pass@host:port/db

# JPA DDL Mode
SPRING_JPA_HIBERNATE_DDL_AUTO=validate

# Profile
SPRING_PROFILES_ACTIVE=dev
```

## Monitoring

### **Check Migration Status:**
```bash
# View application logs
tail -f logs/application.log | grep -i migration

# Check H2 console
http://localhost:8080/api/h2-console
```

### **Verify Tables:**
```sql
-- In H2 console or database client
SELECT table_name FROM information_schema.tables WHERE table_schema = 'PUBLIC';
```

This migration system ensures your database is always in a consistent state while preserving your valuable data. 