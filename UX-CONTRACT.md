# uTernix interaction contract

This document describes existing behavior that the redesign must retain, while implementations change.

- Linux system status reflects real ViewModel state; never substitute a decorative fake status.
- "Configure Linux" opens the existing setup flow; it does not start X11.
- The terminal remains text-only until an explicit graphical launch action.
- Installed applications retain their original exec, permissions, iconFile, pinning, favorites, order, edit actions and uninstall confirmation.
- Store/installer/DEB actions keep existing progress, cancellation and recoverable errors. Never run an installation as a side effect of rendering a screen.
- File browsing does not alter filesystem permissions or move data without a user action.
- Navigation carries the same five destinations across phone dock and tablet rail; no duplicated top tabs.
- Search remains local, immediate and cancellable with an explicit clear action.
- Icons are resolved from the Linux rootfs and rendered without cropping or recoloring. Missing icons use semantic fallbacks only.
- Animations are decorative and cannot block input; respect system animation settings.
- Supported locales: Russian and English; interactive labels and error text belong in Android string resources.
- Buttons should maintain dimensions during progress states and avoid large decorative full-width actions inside status cards.
