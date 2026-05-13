$resDir = "c:\Users\Pongo\AndroidStudio\kaskelas\app\src\main\res\layout"

$files = Get-ChildItem -Path $resDir -Filter *.xml

foreach ($f in $files) {
    $content = Get-Content $f.FullName -Raw
    
    $content = $content -replace '@drawable/glass_bg', '@drawable/bg_glass_card'
    $content = $content -replace '@color/bg_glass_red', '#1AE53935'
    $content = $content -replace '@drawable/ic_edit', '@drawable/ic_nav_settings'
    $content = $content -replace '@drawable/ic_dashboard', '@drawable/ic_nav_home'
    $content = $content -replace '@drawable/ic_group', '@drawable/ic_nav_members'
    
    Set-Content -Path $f.FullName -Value $content
}

Write-Host "Done"
