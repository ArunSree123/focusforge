/** Warm, quiet palette. Colour is used for meaning: moss = progress, saffron = attention, dusk = information. */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        oat: { 50: '#FBF8F2', 100: '#F6F1E8', 200: '#EDE6D8', 300: '#DED4C1', 400: '#C4B8A2' },
        ink: { 900: '#2A2622', 700: '#4A443D', 500: '#6E665C', 400: '#8B8377' },
        moss: { 50: '#EDF3EF', 100: '#D9E7DF', 300: '#93B8A4', 500: '#4E7B66', 600: '#3F6754', 700: '#325143' },
        saffron: { 100: '#F8EBCB', 300: '#E9C476', 500: '#C98F1F', 700: '#8A5F0F' },
        dusk: { 100: '#DDE6EF', 500: '#5F7C9A', 700: '#3F5873' },
        rose: { 100: '#F2DFDC', 500: '#A9615A', 700: '#7A3F3A' },
      },
      fontFamily: {
        display: ['Fraunces', 'Georgia', 'serif'],
        sans: ['"DM Sans"', 'system-ui', 'sans-serif'],
        tamil: ['"Noto Serif Tamil"', 'serif'],
      },
      boxShadow: {
        soft: '0 1px 2px rgba(74,68,61,.05), 0 8px 24px -12px rgba(74,68,61,.18)',
        lift: '0 2px 4px rgba(74,68,61,.06), 0 16px 36px -16px rgba(74,68,61,.28)',
      },
    },
  },
  plugins: [],
}
