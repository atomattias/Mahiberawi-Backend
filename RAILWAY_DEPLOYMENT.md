# Railway Deployment Guide

## Overview
This Spring Boot application is configured to deploy on Railway using Docker.

## Configuration Files

### railway.json
- Uses Dockerfile builder instead of Nixpacks for more reliable builds
- Configured with restart policy for better reliability

### Dockerfile
- Multi-stage build for optimized image size
- Uses Eclipse Temurin JDK 17 (more reliable than OpenJDK)
- Includes health check for better monitoring
- Properly handles JAR file naming

## Environment Variables

Make sure to set these environment variables in Railway:

### Required
- `DATABASE_URL`: PostgreSQL connection string (use Railway's template: `${{ Postgres.DATABASE_URL }}`)
- `JWT_SECRET`: Secure JWT secret key (generate a long, random string)
- `JWT_REFRESH_SECRET`: Secure JWT refresh secret key

### Optional (with defaults)
- `PORT`: Server port (default: 8080)
- `MAIL_HOST`: SMTP host (default: smtp.gmail.com)
- `MAIL_PORT`: SMTP port (default: 587)
- `MAIL_USERNAME`: Email username
- `MAIL_PASSWORD`: Email app password (not regular password)

## Deployment Steps

1. **Connect to Railway**: Link your GitHub repository to Railway
2. **Set Environment Variables**: Add all required environment variables
3. **Deploy**: Railway will automatically build and deploy using the Dockerfile
4. **Monitor**: Check the deployment logs for any issues

## Troubleshooting

### JAR File Not Found
If you see "Unable to access jarfile" errors:
- Check that the Dockerfile is being used (not Nixpacks)
- Verify the JAR file is being built correctly
- Check the build logs in Railway

### Database Connection Issues
- Ensure `DATABASE_URL` is set correctly
- Check that PostgreSQL is provisioned in Railway
- Verify the database is accessible

### Port Issues
- The application uses `server.port=${PORT:8080}` to handle Railway's dynamic port assignment
- No additional configuration needed

### Health Check Failures
- The application includes a health check endpoint at `/health`
- Check that the application starts successfully
- Verify all required environment variables are set

## Testing the Deployment

Once deployed, test these endpoints:
- `GET /` - Root endpoint
- `GET /health` - Health check
- `GET /api/health` - API health check (with context path)

## Common Issues

1. **Build Failures**: Usually due to missing dependencies or compilation errors
2. **Runtime Errors**: Often related to missing environment variables
3. **Database Issues**: Check connection string and database availability
4. **Port Conflicts**: Railway handles this automatically

## Support

If you encounter issues:
1. Check the Railway deployment logs
2. Verify all environment variables are set
3. Test the application locally first
4. Check the application logs for specific error messages 