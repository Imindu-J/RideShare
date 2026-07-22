$base = "http://localhost:8080/api"

function Invoke-Api {
    param(
        [string]$Uri,
        [string]$Method,
        [hashtable]$Headers = @{},
        [string]$Body = $null
    )
    try {
        if ($Body) {
            return Invoke-RestMethod -Uri $Uri -Method $Method -Headers $Headers -ContentType "application/json" -Body $Body -ErrorAction Stop
        } else {
            return Invoke-RestMethod -Uri $Uri -Method $Method -Headers $Headers -ErrorAction Stop
        }
    } catch {
        Write-Host "`n=== REQUEST FAILED ===" -ForegroundColor Red
        Write-Host "URL: $Method $Uri" -ForegroundColor Red
        Write-Host "Status: $($_.Exception.Response.StatusCode.value__) $($_.Exception.Response.StatusCode)" -ForegroundColor Red
        if ($_.Exception.Response) {
            $stream = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($stream)
            Write-Host "Response body:" -ForegroundColor Yellow
            Write-Host $reader.ReadToEnd() -ForegroundColor Yellow
        }
        Write-Host "=======================`n" -ForegroundColor Red
        throw
    }
}

function Register-Or-Login {
    param([string]$Name, [string]$Email, [string]$Password, [string[]]$Roles)
    try {
        $auth = Invoke-RestMethod -Uri "$base/auth/register" -Method POST -ContentType "application/json" -Body (@{
            name = $Name; email = $Email; password = $Password; roles = $Roles
        } | ConvertTo-Json) -ErrorAction Stop
        Write-Host "$Name registered" -ForegroundColor Green
    } catch {
        Write-Host "$Name already exists, logging in instead" -ForegroundColor Yellow
        $auth = Invoke-RestMethod -Uri "$base/auth/login" -Method POST -ContentType "application/json" -Body (@{
            email = $Email; password = $Password
        } | ConvertTo-Json) -ErrorAction Stop
    }
    return $auth
}

# --- Driver ---
$driverAuth = Register-Or-Login -Name "Test Driver" -Email "driver2@test.com" -Password "password123" -Roles @("DRIVER")
$driverToken = $driverAuth.token
$driverHeaders = @{ Authorization = "Bearer $driverToken" }

# --- Create a vehicle ---
$vehicle = Invoke-Api -Uri "$base/users/me/vehicles" -Method POST -Headers $driverHeaders -Body (@{
    model = "Corolla"; licensePlate = "ABC123-$(Get-Random)"; totalSeats = 4
} | ConvertTo-Json)
Write-Host "Vehicle created: $($vehicle.id)" -ForegroundColor Green

# --- List my vehicles ---
$myVehicles = Invoke-Api -Uri "$base/users/me/vehicles" -Method GET -Headers $driverHeaders
Write-Host "My vehicles:" -ForegroundColor Cyan
$myVehicles

# --- Create a ride ---
$ride = Invoke-Api -Uri "$base/rides" -Method POST -Headers $driverHeaders -Body (@{
    vehicleId = $vehicle.id
    origin = "Library"
    destination = "North Campus"
    departureTime = (Get-Date).AddDays(1).ToString("yyyy-MM-ddTHH:mm:ss")
    totalSeats = 3
} | ConvertTo-Json)
Write-Host "Ride created: $($ride.id), status: $($ride.status), seatsAvailable: $($ride.seatsAvailable)" -ForegroundColor Green

# --- Rider ---
$riderAuth = Register-Or-Login -Name "Test Rider" -Email "rider2@test.com" -Password "password123" -Roles @("RIDER")
$riderToken = $riderAuth.token
$riderHeaders = @{ Authorization = "Bearer $riderToken" }

# --- Rider requests a booking ---
$booking = Invoke-Api -Uri "$base/rides/$($ride.id)/bookings" -Method POST -Headers $riderHeaders -Body (@{
    seatsRequested = 1
} | ConvertTo-Json)
Write-Host "Booking requested: $($booking.id), status: $($booking.status)" -ForegroundColor Green

# --- Driver accepts ---
$accepted = Invoke-Api -Uri "$base/bookings/$($booking.id)/accept" -Method PATCH -Headers $driverHeaders
Write-Host "Booking accepted, status: $($accepted.status)" -ForegroundColor Green

# --- Check updated ride ---
$updatedRide = Invoke-Api -Uri "$base/rides/$($ride.id)" -Method GET -Headers $driverHeaders
Write-Host "Ride after accept -> seatsAvailable: $($updatedRide.seatsAvailable), status: $($updatedRide.status)" -ForegroundColor Cyan

# --- Rider's booking history ---
$history = Invoke-Api -Uri "$base/users/me/bookings" -Method GET -Headers $riderHeaders
Write-Host "Rider's booking history:" -ForegroundColor Cyan
$history
