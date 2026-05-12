$baseDir = "c:\Users\Pongo\AndroidStudio\kaskelas\app\src\main\java\com\example\kaskelasapp"
$manifestPath = "c:\Users\Pongo\AndroidStudio\kaskelas\app\src\main\AndroidManifest.xml"

# Map of file -> new package
$fileMap = @{
    "SplashActivity.kt" = "ui.auth"
    "OnboardingActivity.kt" = "ui.auth"
    
    "MainActivity.kt" = "ui.main"
    
    "TambahPemasukanActivity.kt" = "ui.transactions"
    "TambahPengeluaranActivity.kt" = "ui.transactions"
    "DetailPemasukanActivity.kt" = "ui.transactions"
    "DetailPengeluaranActivity.kt" = "ui.transactions"
    
    "RiwayatActivity.kt" = "ui.history"
    "RiwayatAdapter.kt" = "ui.history"
    
    "AnggotaActivity.kt" = "ui.members"
    "TambahAnggotaActivity.kt" = "ui.members"
    "EditAnggotaActivity.kt" = "ui.members"
    "DetailAnggotaActivity.kt" = "ui.members"
    "AnggotaAdapter.kt" = "ui.members"
    "AnggotaBayarAdapter.kt" = "ui.members"
    
    "SettingsActivity.kt" = "ui.settings"
    "ChartDetailActivity.kt" = "ui.chart"
    
    "BackgroundHelper.kt" = "utils"
    "BottomNavHelper.kt" = "utils"
    "CurrencyTextWatcher.kt" = "utils"
    "SpotlightView.kt" = "utils"
    
    "DatabaseHelper.kt" = "data"
    "ModalApp.kt" = "models"
}

# Create directories
$packages = $fileMap.Values | Select-Object -Unique
foreach ($pkg in $packages) {
    $dirPath = Join-Path $baseDir ($pkg -replace "\.", "\")
    if (-not (Test-Path $dirPath)) {
        New-Item -ItemType Directory -Path $dirPath | Out-Null
    }
}

# Move files using git mv
foreach ($file in $fileMap.Keys) {
    $src = Join-Path $baseDir $file
    $pkg = $fileMap[$file]
    $dest = Join-Path $baseDir ($pkg -replace "\.", "\")
    
    if (Test-Path $src) {
        # git mv
        Write-Host "Moving $file to $pkg"
        git mv $src $dest
    }
}

# Update package names in moved files
Write-Host "Updating package declarations..."
Get-ChildItem -Path $baseDir -Recurse -Filter "*.kt" | ForEach-Object {
    $content = Get-Content $_.FullName -Raw
    
    # Determine new package name based on folder structure
    $relPath = $_.DirectoryName.Substring($baseDir.Length).TrimStart('\')
    if ($relPath.Length -gt 0) {
        $newPkg = "com.example.kaskelasapp." + ($relPath -replace "\\", ".")
        # Replace only the first instance of 'package com.example.kaskelasapp'
        if ($content -match "^package com\.example\.kaskelasapp$") {
            # Note: Regex ^ might not work with multiline easily without inline modifier, so:
            $content = $content -replace 'package com\.example\.kaskelasapp', "package $newPkg"
        }
        
        # We need to add imports to models and utils since they are moved
        $importsToAdd = "`nimport com.example.kaskelasapp.models.*`nimport com.example.kaskelasapp.utils.*`nimport com.example.kaskelasapp.data.*`n"
        $content = $content -replace "package $newPkg", "package $newPkg`n$importsToAdd"
        
        Set-Content -Path $_.FullName -Value $content
    }
}

# Update AndroidManifest.xml
Write-Host "Updating AndroidManifest.xml..."
$manifestContent = Get-Content $manifestPath -Raw
foreach ($file in $fileMap.Keys) {
    if ($file -match "Activity\.kt$") {
        $activityName = $file.Replace(".kt", "")
        $pkg = $fileMap[$file]
        $manifestContent = $manifestContent -replace "`"$activityName`"", "`".$pkg.$activityName`" "
        # Also handle .ActivityName
        $manifestContent = $manifestContent -replace "`"\.$activityName`"", "`".$pkg.$activityName`""
    }
}
Set-Content -Path $manifestPath -Value $manifestContent

Write-Host "Done!"
