# Alias drive menghindari masalah compiler Kotlin dengan koma pada path Windows.
# Contoh: .\run.ps1 :desktopApp:run
[CmdletBinding()]
param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]] $GradleTasks = @(':desktopApp:run')
)

$ErrorActionPreference = 'Stop'
$projectDirectory = $PSScriptRoot
$mappedDrive = $null
$buildExitCode = 1
$previousDebugValue = $env:DEBUG

try {
    if ($projectDirectory.Contains(',')) {
        foreach ($letter in @('P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z')) {
            if (-not (Test-Path -LiteralPath "${letter}:\")) {
                & subst "${letter}:" $projectDirectory
                if ($LASTEXITCODE -ne 0) { throw 'Tidak dapat membuat alias drive untuk proyek.' }
                $mappedDrive = "${letter}:"
                $projectDirectory = "${letter}:\"
                break
            }
        }
        if (-not $mappedDrive) { throw 'Tidak ada drive kosong. Salin proyek ke path tanpa tanda koma.' }
    }
    $env:DEBUG = $null
    Push-Location -LiteralPath $projectDirectory
    try {
        & (Join-Path $projectDirectory 'gradlew.bat') @GradleTasks --console=plain
        $buildExitCode = $LASTEXITCODE
    } finally {
        Pop-Location
    }
} finally {
    $env:DEBUG = $previousDebugValue
    if ($mappedDrive) { & subst $mappedDrive /d }
}

exit $buildExitCode
