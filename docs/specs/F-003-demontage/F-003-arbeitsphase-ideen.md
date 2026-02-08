# F-003 Arbeitsphase - Ideensammlung

**Status**: Konzeptphase - keine formale Spec

Dieser Bereich sammelt Ideen und Anforderungen für die Erweiterung der "Arbeitsphase" (aktuell nur "Ausgebaut"-Button).

## Vision

Die Arbeitsphase ist der Zeitraum zwischen "Bauteil fotografiert" und "nächste Aktion gewählt". Hier könnte der Mechaniker:
- Zeiterfassung steuern (Timer, Pause)
- Kommentare hinzufügen (Text, Voice, Voice-to-Text)
- Notizen zu speziellen Herausforderungen machen

## Ideen

### Zeiterfassung
- Timer läuft automatisch ab Foto-Bestätigung
- "Pause"-Button stoppt Timer (z.B. für Werkzeug holen, Anruf)
- Timer läuft weiter bis "Ausgebaut" gedrückt wird
- Zeiten pro Schritt werden gespeichert (→ spätere Kalkulation)

### Kommentare
- **Text**: Freitext-Eingabe für Notizen
- **Voice**: Sprachaufnahme für dreckige Hände
- **Voice-to-Text**: STT für durchsuchbare Notizen

Frage: Kommentar pro Schritt oder global pro Vorgang?

### Pausieren
- "Pause"-Button → Timer stoppt, App kann verlassen werden
- Beim Zurückkommen: "Fortsetzen"-Button
- State Recovery: Zuletzt aufgenommenes Foto wird angezeigt

### UI-Ideen
- Großer "Ausgebaut"-Button bleibt
- Kleinere Sekundär-Buttons: Pause, Kommentar, ...
- Timer-Anzeige im Hintergrund (nicht aufdringlich)

## Offene Fragen
- [ ] Ist Pausieren Feature-spezifisch oder app-weit? (Lifecycle-Concern?)
- [ ] Kommentar obligatorisch oder optional?
- [ ] Voice-Recordings: Wie viel Speicher ist akzeptabel?
- [ ] Sollte Zeiterfassung eine separate Spec sein (F-005) oder Teil der Arbeitsphase?

## Abhängigkeiten
- CameraX (bereits da)
- Android Speech Recognition API (für Voice-to-Text)
- Room DB: `comments` Tabelle, `steps.pausedDuration` Feld

## Nächste Schritte
- MVP testen: Reicht der "Ausgebaut"-Button oder brauchen Mechaniker sofort Kommentare?
- User Feedback sammeln
- Dann formale Spec schreiben
