# Cerca Madrid - Design decisions

This document records the main design decisions of the frontend and **why** each one was taken, including the alternatives that were rejected.

## 1. Brand

- **Name:** Cerca Madrid ("Close to you, Madrid").
- **Tagline:** "Ayuda abierta ahora en Madrid" (Help open now in Madrid).
- **Values:** dignified, warm, reliable.
- **Tone of voice:** informal "tú", short sentences, no jargon. Why: the people using the app may be stressed, tired or have limited Spanish; plain and close language is easier to understand and less institutional.

### Name check

Web searches found no existing app or organisation called "Cerca Madrid". This is not a trademark search; it would have to be done properly before any commercial use.

The City Council of Madrid has its own app and strategy called **Dignitas** (launched December 2025). Its name and wording are avoided on purpose to prevent confusion.

### Why this project still makes sense

Dignitas covers similar ground, so the differentiator is explicit: Cerca Madrid answers one question, "what is open **right now**?", with no registration and a very light page.

## 2. Colour palette

| Role              | Hex                   | Use                         |
| ----------------- | --------------------- | --------------------------- |
| Teal (primary)    | `#0D6B6E`             | header line, buttons, links |
| Teal dark         | `#094F52`             | titles, pressed states      |
| Teal tint         | `#DDF0EE`             | "open now" badge background |
| Coral (accent)    | `#F26B5B`             | logo heart, decoration      |
| Coral tint / text | `#FDE7E3` / `#7A2519` | "opens soon" badge          |
| Background        | `#F6F4EF`             | warm cream page background  |
| Card              | `#FFFFFF`             | place cards                 |
| Text              | `#1C2B2E`             | main text                   |
| Muted text        | `#4A5C60`             | addresses, secondary info   |
| Border            | `#D6D1C4`             | card and badge borders      |

### Contrast (WCAG AA needs 4.5:1 for normal text)

- Text on background: 13.3
- Text on card: 14.6
- Muted text on card: 7.0
- White on teal (buttons): 6.3
- Dark text on coral: 4.9

**Rule:** white text is never placed on coral (contrast 3.0, fails AA). Coral is used for graphics and decoration only.

### Why teal + coral

Three directions were compared: blue + amber, teal + coral, terracotta + cream.

- Teal + coral was chosen: calm, warm, not institutional and clearly different from municipal apps.
- Blue + amber was too close to a typical institutional look.
- Terracotta was the hardest one to make accessible.
- Red and "charity" clichés were avoided on purpose.

## 3. Typography

**Atkinson Hyperlegible**, regular (400) and bold (700).

- Designed by the Braille Institute for low-vision readers: letters such as `l`, `I`, `1`, `0` and `O` are clearly distinct.
- **Self-hosted** through the `@fontsource/atkinson-hyperlegible` package: no request to Google Fonts (privacy, speed, works with poor connection).
- Weight: only the latin subset is loaded for Spanish text, about 35 kB of woff2 files in total.

### Slashed zero (decision kept on purpose)

The font draws zeros with a slash (`12:00`). In times and dates a zero can never be the letter O, so the benefit is small there, and it looks unusual. Rejected alternative: a second font only for numbers. Kept option: one typeface, because it keeps the accessibility story coherent.

## 4. Logo and favicon

- **Main logo:** a hand holding a heart, teal hand and coral heart, no text.
- **Favicon:** the same symbol inside a teal map pin (white hand, light coral heart), which joins "place" and "help" and stays legible at small sizes.
- **Origin:** the hand-heart drawing comes from the Lucide icon `hand-heart` (ISC licence), recoloured. The pin shape is original.
- **Discarded:** hand-drawn two-hands versions made by code. They looked crude, and drawing hands well is a job for an illustrator or a professional icon.
- **Header:** cream background with a 3 px teal line (warm, lets the place cards take the focus) was preferred over a solid teal header.

## 5. Icons

`lucide-react` line icons (ISC licence) replace emoji. Why: emoji look different on every device and give an informal impression; line icons inherit the text colour and are consistent everywhere.

| Meaning      | Icon         |
| ------------ | ------------ |
| All          | `MapPin`     |
| Eat          | `Soup`       |
| Sleep        | `BedDouble`  |
| Shower       | `ShowerHead` |
| Clothes      | `Shirt`      |
| Nothing open | `Clock`      |

All icons are decorative (`aria-hidden`); the label next to each one carries the meaning.

## 6. Interface decisions

- **Filters:** pill buttons with `aria-pressed`. The selected one also shows a check mark, so meaning does not rely on colour alone.
- **Cards:** type badge and "Abierto ahora" badge first (text, not only colour), then name, address, bold hours for today, directions button, and the update date small at the end. The goal is that a person in a hurry sees what it is, if it is open, and until when.
- **Empty state:** when nothing is open, the screen shows what opens next (name, address, when) and a directions button, instead of a dead end. This is likely the most used screen at night.
- **Directions:** a link to Google Maps with the destination only. The app never asks for the user's location.
- **Touch targets:** at least 44 px high.
- **Focus:** a thick, high-contrast outline on keyboard focus.

## 7. Verification

Measured with Lighthouse (mobile) on the production build:

| Category       | Score |
| -------------- | ----- |
| Accessibility  | 100   |
| Best Practices | 100   |
| SEO            | 100   |
| Performance    | 99    |

- Keyboard-only test passed (tab order, focus visible, filters and links usable).
- Size: about 110 kB in total (JavaScript 72 kB gzip, CSS 1.3 kB gzip, fonts about 35 kB).
- Lighthouse results are an **automatic** check and cover only part of WCAG; it does not replace testing with real people or a screen reader.
- SEO was first 83, then 92, then 100: a missing `meta description`, then a missing `robots.txt` (the dev server returned the page instead).
- Performance 55 was measured on the Vite dev server and is not meaningful; the number above is from the production build.

### Decision not taken

Lighthouse suggests reducing unused JavaScript (about 35 kB, mostly React internals, not scored). Code splitting was **not** done: the app is a single screen, so the added complexity would not pay off.

## 8. Pending

- Responsive grid of cards for wide screens (mobile layout already works).
- Dark mode (could save battery on OLED screens).
- Real data replacing the TEST data, with attribution (see README).
- Test with real users and a screen reader.

## 9. Credits

- Icons and hand-heart logo drawing: [Lucide](https://lucide.dev), ISC licence.
- Font: Atkinson Hyperlegible (Braille Institute), SIL Open Font License, via Fontsource.
