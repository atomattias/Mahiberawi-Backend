# Debug Login Response Script
Write-Host "Debugging login response" -ForegroundColor Green
Write-Host "=======================" -ForegroundColor Green

$baseUrl = "https://mahiberawi-backend-production.up.railway.app/api"
$email = "mattiasgebrie@gmail.com"

Write-Host "`nDebugging user: $email" -ForegroundColor Yellow
Write-Host "Backend URL: $baseUrl" -ForegroundColor Cyan

# Authenticate and get detailed response
Write-Host "`nAuthenticating and analyzing response..." -ForegroundColor Yellow

# Prompt for password
$password = Read-Host "Enter password for $email" -AsSecureString
$passwordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($password))

try {
    $loginBody = @{
        email = $email
        password = $passwordPlain
    } | ConvertTo-Json

    $loginHeaders = @{
        "Content-Type" = "application/json"
    }

    $loginResponse = Invoke-WebRequest -Uri "$baseUrl/auth/login" -Method POST -Body $loginBody -Headers $loginHeaders
    Write-Host "SUCCESS - Authentication: $($loginResponse.StatusCode)" -ForegroundColor Green
    
    Write-Host "`nFull Login Response:" -ForegroundColor Cyan
    Write-Host $loginResponse.Content -ForegroundColor White
    
    $loginData = $loginResponse.Content | ConvertFrom-Json
    $jwtToken = $loginData.data.accessToken
    
    Write-Host "`nJWT Token (first 50 chars): $($jwtToken.Substring(0, 50))..." -ForegroundColor Yellow
    
    # Try to decode JWT token (basic info)
    $tokenParts = $jwtToken.Split('.')
    if ($tokenParts.Length -eq 3) {
        Write-Host "`nJWT Token Structure:" -ForegroundColor Cyan
        Write-Host "Header: $($tokenParts[0])" -ForegroundColor White
        Write-Host "Payload: $($tokenParts[1])" -ForegroundColor White
        Write-Host "Signature: $($tokenParts[2].Substring(0, 20))..." -ForegroundColor White
        
        # Try to decode payload (base64)
        try {
            $payloadBytes = [System.Convert]::FromBase64String($tokenParts[1])
            $payloadJson = [System.Text.Encoding]::UTF8.GetString($payloadBytes)
            Write-Host "`nDecoded Payload:" -ForegroundColor Cyan
            Write-Host $payloadJson -ForegroundColor White
        } catch {
            Write-Host "Could not decode JWT payload" -ForegroundColor Red
        }
    }
    
} catch {
    Write-Host "FAILED - Authentication: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $errorResponse = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($errorResponse)
        $errorContent = $reader.ReadToEnd()
        Write-Host "Error Details: $errorContent" -ForegroundColor Red
    }
}

Write-Host "`nDebug complete!" -ForegroundColor Green 