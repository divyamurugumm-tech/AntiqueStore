# PowerShell script to test all REST API endpoints
$baseUrl = "http://localhost:8080"

Write-Host "==============================================" -ForegroundColor Cyan
Write-Host " Testing Antique Collectibles Store REST API " -ForegroundColor Cyan
Write-Host "==============================================" -ForegroundColor Cyan

function Test-Endpoint {
    param(
        [string]$Title,
        [string]$Method,
        [string]$Url,
        [string]$Body = $null
    )
    Write-Host "`n>>> $Title" -ForegroundColor Yellow
    Write-Host "$Method $Url" -ForegroundColor Gray
    try {
        $headers = @{ "Content-Type" = "application/json" }
        if ($Body) {
            $response = Invoke-RestMethod -Uri $Url -Method $Method -Headers $headers -Body $Body
        } else {
            $response = Invoke-RestMethod -Uri $Url -Method $Method -Headers $headers
        }
        $response | ConvertTo-Json -Depth 5
    } catch {
        Write-Host "Status Code: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
        if ($_.ErrorDetails.Message) {
            Write-Host $_.ErrorDetails.Message -ForegroundColor DarkRed
        }
    }
}

# 1. GET /api/eras
Test-Endpoint -Title "1. GET /api/eras (List all eras)" -Method "GET" -Url "$baseUrl/api/eras"

# 2. POST /api/eras
$newEra = @{
    eraName = "Edwardian"
    startYear = 1901
    endYear = 1914
} | ConvertTo-Json
Test-Endpoint -Title "2. POST /api/eras (Create new era)" -Method "POST" -Url "$baseUrl/api/eras" -Body $newEra

# 3. GET /api/items
Test-Endpoint -Title "3. GET /api/items (List all items from DB)" -Method "GET" -Url "$baseUrl/api/items"

# 4. GET /api/items/1 (via BST)
Test-Endpoint -Title "4. GET /api/items/1 (Search item 1 via BST)" -Method "GET" -Url "$baseUrl/api/items/1"

# 5. GET /api/items/3 (item 3 is SOLD -> 404 from BST)
Test-Endpoint -Title "5. GET /api/items/3 (Search item 3 - SOLD, expect 404 from BST)" -Method "GET" -Url "$baseUrl/api/items/3"

# 6. GET /api/items/sorted (BST in-order)
Test-Endpoint -Title "6. GET /api/items/sorted (BST in-order traversal)" -Method "GET" -Url "$baseUrl/api/items/sorted"

# 7. GET /api/items/range (BST range search)
Test-Endpoint -Title "7. GET /api/items/range?min=50000&max=200000 (BST price range search)" -Method "GET" -Url "$baseUrl/api/items/range?min=50000&max=200000"

# 8. POST /api/items (Create new item -> writes to DB and BST)
$newItem = @{
    name = "Baroque Gilded Mirror"
    category = "Decor"
    eraId = 2
    condition = "Excellent"
    price = 125000.00
    status = "AVAILABLE"
} | ConvertTo-Json
Test-Endpoint -Title "8. POST /api/items (Create new available item)" -Method "POST" -Url "$baseUrl/api/items" -Body $newItem

# 9. PUT /api/items/1 (Update item in DB and BST)
$updateItem = @{
    name = "Brass Astrolabe (Restored)"
    category = "Instruments"
    eraId = 2
    condition = "Restored"
    price = 195000.00
    status = "AVAILABLE"
} | ConvertTo-Json
Test-Endpoint -Title "9. PUT /api/items/1 (Update item)" -Method "PUT" -Url "$baseUrl/api/items/1" -Body $updateItem

# 10. POST /api/valuations
$newValuation = @{
    itemId = 1
    appraisedValue = 195000.00
    valDate = "2026-11-20"
    appraiser = "H. Vance"
} | ConvertTo-Json
Test-Endpoint -Title "10. POST /api/valuations (Create valuation)" -Method "POST" -Url "$baseUrl/api/valuations" -Body $newValuation

# 11. GET /api/items/1/valuations
Test-Endpoint -Title "11. GET /api/items/1/valuations (List valuations for item 1)" -Method "GET" -Url "$baseUrl/api/items/1/valuations"

# 12. POST /api/sales (Sell item 2 -> marks SOLD, removes from BST)
$newSale = @{
    itemId = 2
    buyerName = "Eleanor Vance"
    salePrice = 240000.00
    saleDate = "2026-11-20"
} | ConvertTo-Json
Test-Endpoint -Title "12. POST /api/sales (Record sale for item 2)" -Method "POST" -Url "$baseUrl/api/sales" -Body $newSale

# 13. GET /api/items/2 (Verify item 2 is now removed from BST -> 404)
Test-Endpoint -Title "13. GET /api/items/2 (Verify item 2 now returns 404 from BST)" -Method "GET" -Url "$baseUrl/api/items/2"

# 14. GET /api/sales (Sales report with item names and total revenue)
Test-Endpoint -Title "14. GET /api/sales (Sales report with item names and total revenue)" -Method "GET" -Url "$baseUrl/api/sales"

# 15. DELETE /api/items/12 (Delete item from DB and BST)
Test-Endpoint -Title "15. DELETE /api/items/12 (Delete item 12)" -Method "DELETE" -Url "$baseUrl/api/items/12"

# 16. Validation error test (Missing required fields & negative price -> expect 400 Bad Request)
$invalidItem = @{
    name = ""
    category = ""
    eraId = 1
    condition = "Good"
    price = -50.00
} | ConvertTo-Json
Test-Endpoint -Title "16. POST /api/items with invalid data (Expect 400 validation error)" -Method "POST" -Url "$baseUrl/api/items" -Body $invalidItem

Write-Host "`n==============================================" -ForegroundColor Cyan
Write-Host " All endpoint tests complete!                " -ForegroundColor Cyan
Write-Host "==============================================" -ForegroundColor Cyan
