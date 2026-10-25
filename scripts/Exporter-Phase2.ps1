param(
    [string]$Processing = 'C:\Program Files\Processing',
    [string]$Destination = ''
)
$ErrorActionPreference = 'Stop'
$racine = Split-Path -Parent $PSScriptRoot
if (-not $Destination) {
    $Destination = Join-Path $racine ('release\Phase2-' + (Get-Date -Format 'yyyyMMdd-HHmmss'))
}
$Destination = [System.IO.Path]::GetFullPath($Destination)
if (Test-Path -LiteralPath $Destination) {
    throw "Le dossier existe déjà : $Destination. Choisir une nouvelle destination."
}
$ressources = Join-Path $Processing 'app\resources'
$java = Join-Path $ressources 'jdk\bin\java.exe'
if (-not (Test-Path -LiteralPath $java)) {
    throw 'Java fourni par Processing introuvable. Indiquer -Processing avec le dossier de Processing 4.'
}
Push-Location $racine
try {
    & mvn -B package
    if ($LASTEXITCODE -ne 0) { throw 'Échec du build Maven.' }
    $equipe = Join-Path $Destination 'Groupe 2 - Sonic'
    New-Item -ItemType Directory -Path $equipe | Out-Null
    $application = Join-Path $equipe 'Application'
    & $java "-Dcompose.application.resources.dir=$ressources" -cp (Join-Path $Processing 'app\*') `
        processing.mode.java.Commander "--sketch=$racine\src\sketch\Blob" `
        "--output=$application" --variant=windows-amd64 --export
    if ($LASTEXITCODE -ne 0 -or -not (Test-Path -LiteralPath (Join-Path $application 'Blob.exe'))) {
        throw "Échec de l'export Processing. Les fichiers partiels sont conservés dans $Destination."
    }
    foreach ($dossier in @('Sources', 'Docs', 'Files')) {
        New-Item -ItemType Directory -Path (Join-Path $equipe $dossier) | Out-Null
    }
    Copy-Item -LiteralPath (Join-Path $racine 'src') -Destination (Join-Path $equipe 'Sources\src') -Recurse
    Copy-Item -LiteralPath (Join-Path $racine 'scripts') -Destination (Join-Path $equipe 'Sources\scripts') -Recurse
    Copy-Item -LiteralPath (Join-Path $racine 'pom.xml'), (Join-Path $racine 'README.md') -Destination (Join-Path $equipe 'Sources')
    Copy-Item -LiteralPath (Join-Path $racine 'Docs\Phase2-Formules-et-oral.md') -Destination (Join-Path $equipe 'Docs')
    foreach ($document in @('Docs\Depassement-Phase2.md', 'Docs\Presentation-Phase2-Niveau\Presentation-Phase2-Niveau.pdf', 'Docs\Presentation-Phase2-Niveau\Presentation-Phase2-Niveau-Corrigee.pptx')) {
        $sourceDocument = Join-Path $racine $document
        if (Test-Path -LiteralPath $sourceDocument) {
            Copy-Item -LiteralPath $sourceDocument -Destination (Join-Path $equipe 'Docs')
        }
    }
    @'
Avant le dépôt Moodle : personnaliser la présentation PDF de phase 2 et ajouter
dans Files la vidéo de démonstration phase 2 (2-3 min). Les médias de phase 1
ne sont pas recopiés. Tester Application/Blob.exe, puis zipper le dossier
Groupe 2 - Sonic et vérifier que le ZIP fait moins de 500 Mo.
'@ | Set-Content -LiteralPath (Join-Path $equipe 'A-finaliser.txt') -Encoding utf8
    Write-Host "Application exportée dans $application"
    Write-Warning 'Avant le ZIP final : vérifier les diapositives, démontrer le niveau et ajouter la vidéo phase 2.'
} finally {
    Pop-Location
}
