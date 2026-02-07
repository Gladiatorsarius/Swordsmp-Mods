````markdown
# Solid Displays - Item Display HP System

## Übersicht

Dieses Mod-Template bietet ein vollständiges HP-System für Item Displays in Minecraft 1.21+ mit Fabric.
✓ **GameRule-Unterstützung**: `solid_displays` kann zum Aktivieren/Deaktivieren des Systems verwendet werden
## Features

✓ **GameRule-Unterstützung**: `solid_displays` kann zum Aktivieren/Deaktivieren des Systems verwendet werden
✓ **NBT-Persistierung**: HP-Daten werden gespeichert und beim Laden wiederhergestellt
### `solid_displays`
- **Standard**: true
- **Beschreibung**: Aktiviert das Item Display HP-System
- **Verwendung**: `/gamerule solid_displays true`
✓ **Physische Hitbox**: Item Displays haben eine physische Hitbox und können mit Waffen angegriffen werden
✓ **Schaden von allen Quellen**: Unterstützt Nahkampf, Projektile, Explosionen, Feuer, etc.
```
/gamerule solid_displays true
```
## GameRule
### 1. System aktivieren
```
/gamerule solid_displays true
```
### Commands funktionieren nicht
- Stelle sicher, dass du OP-Rechte hast (`/op @s`)
-- Stelle sicher, dass die GameRule aktiviert ist: `/gamerule solid_displays true`

### `/displayhp set <target> <hp>`
Setzt die HP eines Item Displays auf einen spezifischen Wert (1-1000).

**Beispiel:**
```
/displayhp set @e[type=item_display] 500
```

### `/displayhp get <target>`
Gibt die aktuelle und maximale HP eines Item Displays aus.

**Beispiel:**
```
/displayhp get @e[type=item_display,limit=1]
```

### `/displayhp damage <target> <damage>`
Sendet Schaden an ein Item Display. Wenn HP auf 0 oder niedriger sinkt, wird das Display gelöst und das Item fällt herunter.

**Beispiel:**
```
/displayhp damage @e[type=item_display,limit=1] 50.5
```

## Verwendungsbeispiele

### 1. System aktivieren
```
/gamerule solid_displays true
```

### 2. Item Display mit HP erstellen
```
/summon item_display ~ ~ ~ {item:{id:"diamond",count:1}}
/displayhp set @e[type=item_display,sort=nearest,limit=1] 100
```

### 3. Display mit Schwert angreifen
```
# Item Display spawnen
/summon item_display ~ ~ ~ {item:{id:"diamond",count:1}}

# Jetzt kannst du das Display mit einem Schwert oder einer anderen Waffe schlagen!
# Es wird automatisch 100 HP haben und Schaden nehmen
```

### 4. Display mit Befehl beschädigen
```
/displayhp damage @e[type=item_display,sort=nearest,limit=1] 25
```

### 5. HP auslesen
```
/displayhp get @e[type=item_display,sort=nearest,limit=1]
```

## Technische Details

### Komponenten

1. **ItemDisplayHPManager.java**
   - Verwaltet alle HP-Daten von Item Displays
   - Speichert/lädt Daten in/aus NBT
   - Verwaltet Item-Drops bei Death
   - Auto-Initialisierung mit 100 HP

2. **ItemDisplayHPCommand.java**
   - Registriert alle in-game Commands
   - Validiert Eingaben und Ziele
   - Prüft GameRule-Status

3. **ItemDisplayEventHandler.java**
   - Lädt HP-Daten beim Entity-Laden
   - Räumt auf beim Entity-Entladen

4. **ItemDisplayHPGamerule.java**
   - Registriert die GameRule
   - Stellt Aktivierungsstatus bereit

5. **ItemDisplayMixin.java**
   - Verarbeitet Item Display Initialisierung
   - Stellt sicher, dass NBT-Daten geladen werden

6. **ItemDisplayDamageHandler.java**
   - Verarbeitet Angriffe von Spielern
   - Berechnet Waffenschaden
   - Erzeugt visuelle und akustische Effekte

7. **ItemDisplayHitboxMixin.java**
   - Gibt Item Displays eine physische Hitbox (0.75x0.75 Blöcke)
   - Macht sie verletzbar durch alle Schadensquellen

## Physische Kampfmechanik

### Hitbox
Item Displays haben eine **0.75x0.75 Blöcke große Hitbox**, wenn das HP-System aktiviert ist. Das ist größer als normale Items, aber kleiner als die meisten Entities, was sie zu einem ausgewogenen Ziel macht.

### Unterstützte Schadensquellen
- ⚔️ **Nahkampfwaffen**: Schwerter, Äxte, Fäuste
- 🏹 **Projektile**: Pfeile, Dreizacke
- 💥 **Explosionen**: TNT, Creeper, etc.
- 🔥 **Feuer und Lava**n+- ⚡ **Alle anderen Vanilla-Schadensquellen**

### Schadensberechnung
Der Schaden wird basierend auf der verwendeten Waffe berechnet:
- **Faust**: 1 Schaden
- **Holzschwert**: 4 Schaden
- **Steinschwert**: 5 Schaden
- **Eisenschwert**: 6 Schaden
- **Diamantschwert**: 7 Schaden
- **Netherschwert**: 8 Schaden
- **Kritischer Treffer**: 1.5x Multiplikator (wenn Spieler fällt)

### Automatische HP-Initialisierung
Wenn das HP-System aktiviert ist, erhalten neu platzierte Item Displays automatisch **100 HP**. Du kannst dies mit `/displayhp set` anpassen.

### Visuelle Effekte
Beim Angreifen eines Item Displays siehst du:
- 🎯 **Schadenpartikel** (rote Kreuze)
- 🔊 **Treffergeräusch** (Angriffssound)

## HP-Bereiche

- **Minimum HP**: 1
- **Maximum HP**: 1000
- **Item Drop**: Automatisch wenn HP ≤ 0

## NBT-Struktur

Item Displays speichern ihre HP in den folgenden NBT-Schlüsseln:
```
solidDisplays:hp       - Aktuelle HP
solidDisplays:maxHp    - Maximale HP
```

## Berechtigungen

Alle Commands erfordern OP-Level 2 (Operator-Berechtigungen).

## Kompatibilität

- **Minecraft Version**: 1.21+
- **Loader**: Fabric
- **Java Version**: Java 21+

## Installation

1. Entpacke das Mod-Template
2. Führe `./gradlew build` aus (Windows: `gradlew.bat build`)
3. Die JAR-Datei befindet sich in `build/libs/`
4. Kopiere die JAR in den Mods-Ordner

## Troubleshooting

### Commands funktionieren nicht
- Stelle sicher, dass du OP-Rechte hast (`/op @s`)
-- Stelle sicher, dass die GameRule aktiviert ist: `/gamerule solid_displays true`

### HP-Daten gehen verloren
- NBT-Daten werden automatisch gespeichert
- Stelle sicher, dass du das Display mit den Commands setzt, nicht von Hand

### Item wird nicht gedropt
- Stelle sicher, dass das Display ein Item hat
- Überprüfe, ob das Level auf dem Server lädt

## Lizenz

CC0-1.0

## Autoren

Basierend auf dem Fabric Example Mod Template

````
