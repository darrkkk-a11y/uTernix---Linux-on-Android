# uTernix — Chromatic Motion

## Direction

uTernix brings graphical Linux applications and command-line tools to Android. Its interface should feel like a living application workspace, not a traditional settings utility. The focal point is the **real artwork of installed Linux applications**, preserved at its natural aspect ratio.

Design register: functional software tool with precise, expressive visuals. Avoid monochrome grids, fake logos, gratuitous glass, constant animation, and full-width primary buttons on every card. Use meaningful color to distinguish terminal, files, packages, sessions and error states.

## Canonical tokens

The runtime source of truth is `tessera-linux/app/src/main/java/com/daryan/tessera/ui/deb/UternixTheme.kt` (`UColors`, `UTypography`, `UShapes`). This file documents those tokens; update it alongside runtime changes.

| Purpose | Token | Hex |
|---|---|---|
| Background | UColors.Background | #080D1B |
| Panel | UColors.Surface | #151E31 |
| Raised interactive | UColors.Raised | #1E2C45 |
| Brand/action | UColors.Accent | #91B2FF |
| Terminal/success | UColors.Success | #76E8B9 |
| Files/info | UColors.Info | #66D9F4 |
| Packages | UColors.Package | #FFB578 |
| Sessions/system | UColors.System | #CAABFF |
| Creative/media | UColors.Rose | #FF91D2 |
| Error/destructive | UColors.Danger | #FF849F |
| Text | UColors.Text | #F8FAFF |

Typography: Onest body, Onest semibold display; terminal output uses the terminal's configured monospace font. Corner radius: 12dp interactive controls, 18–22dp structural cards. Space: 8/12/16/20dp.

## Components and motion

`UternixMotion.kt` owns the common atmospheric background and pressed elevation/scale. `UternixScreenKit.kt` and `UternixSurfaces.kt` own panels and action rows. Prefer one owner over screen-local copies.

- Control press: spring scaling ~0.965, shadow reduction, no spring on idle.
- Navigation: animated selected states only, not continuous movement.
- Enter: short 340ms reveal on the main workspace.
- Respect Android's reduced-motion setting; content does not depend on animation.
- No animated effects on terminal content or data-heavy lists while idle.

## Linux icons

`SystemApps` resolves `.desktop` Icon= entries through PNG/SVG/XPM paths, including scalable system themes. `AppGlyph` decodes asynchronously, caches by source timestamp and renders with `ContentScale.Fit`. Native colored artwork always wins over fallback symbols; never recolor it. Only when an icon is genuinely missing use a category-specific symbolic fallback. A custom background may surround real artwork but must never replace it.

## Layout rules

Phone: dock navigation, adaptive 144dp library columns, 74dp kinetic quick actions, 74dp library artwork. Wide screens: side navigation, 2-column dashboard and independent scrolling regions. Status card has a content-width 40dp Linux action, never a full-width feature banner. Touch targets >= 40dp with 48dp preferred; ensure content descriptions and RU/EN labels.

## Quality bar

For each changed route test real assets, pending and missing states, loading, error handling, long Russian strings, 360dp viewport, tablet landscape, and reduced motion. An APK build alone does not prove these behaviors; confirm on device before calling it a stable release.
