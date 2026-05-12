$baseDir = "c:\Users\Pongo\AndroidStudio\kaskelas"
$resDir = "$baseDir\app\src\main\res"

$renames = @(
    @{ Type="layout"; Old="animated_background"; New="layout_animated_background"; Ext=".xml" },
    @{ Type="layout"; Old="bottom_nav"; New="layout_bottom_nav"; Ext=".xml" },
    @{ Type="drawable"; Old="background"; New="bg_main"; Ext=".xml" },
    @{ Type="drawable"; Old="chart_gradient"; New="bg_chart_gradient"; Ext=".xml" },
    @{ Type="drawable"; Old="nav_history"; New="ic_nav_history"; Ext=".xml" },
    @{ Type="drawable"; Old="nav_home"; New="ic_nav_home"; Ext=".xml" },
    @{ Type="drawable"; Old="nav_members"; New="ic_nav_members"; Ext=".xml" },
    @{ Type="drawable"; Old="nav_settings"; New="ic_nav_settings"; Ext=".xml" },
    @{ Type="drawable"; Old="splashscreen"; New="img_splashscreen"; Ext=".png" },
    @{ Type="drawable"; Old="text_radio_selector"; New="bg_text_radio_selector"; Ext=".xml" }
)

# Move files using git mv
foreach ($item in $renames) {
    $src = Join-Path $resDir "$($item.Type)\$($item.Old)$($item.Ext)"
    $dest = Join-Path $resDir "$($item.Type)\$($item.New)$($item.Ext)"
    if (Test-Path $src) {
        Write-Host "Moving $src to $dest"
        git mv $src $dest
    }
}

# Update references in all code files
$filesToProcess = Get-ChildItem -Path "$baseDir\app\src\main" -Recurse -Include *.xml,*.kt -File

foreach ($file in $filesToProcess) {
    $content = Get-Content $file.FullName -Raw
    $modified = $false
    
    foreach ($item in $renames) {
        $type = $item.Type
        $old = $item.Old
        $new = $item.New
        
        # XML references (e.g. @layout/bottom_nav or @drawable/background)
        $xmlRefOld = "@$type/$old"
        $xmlRefNew = "@$type/$new"
        if ($content -match $xmlRefOld) {
            $content = $content -replace $xmlRefOld, $xmlRefNew
            $modified = $true
        }
        
        # Kotlin references (e.g. R.layout.bottom_nav or R.drawable.background)
        $ktRefOld = "R\.$type\.$old\b"
        $ktRefNew = "R.$type.$new"
        if ($content -match $ktRefOld) {
            $content = $content -replace $ktRefOld, $ktRefNew
            $modified = $true
        }
    }
    
    if ($modified) {
        Set-Content -Path $file.FullName -Value $content
        Write-Host "Updated references in $($file.Name)"
    }
}

Write-Host "Rename and update completed."
