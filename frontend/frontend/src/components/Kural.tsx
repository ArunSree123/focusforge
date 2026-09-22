import { motion, useReducedMotion } from 'framer-motion'

/** The closing signature of the app. Deliberately not a card: centred, lots of air, a faint warm glow. */
export function Kural() {
  const reduce = useReducedMotion()
  return (
    <motion.footer
      initial={reduce ? false : { opacity: 0 }} whileInView={{ opacity: 1 }} viewport={{ once: true }} transition={{ duration: 2.4, ease: 'easeOut' }}
      className="relative mx-auto mt-24 mb-12 max-w-2xl px-6 text-center" aria-label="Thirukkural">
      <div aria-hidden className="pointer-events-none absolute left-1/2 top-1/2 h-40 w-[26rem] max-w-full -translate-x-1/2 -translate-y-1/2 rounded-full bg-saffron-300/25 blur-3xl" />
      <p lang="ta" className="relative font-tamil text-xl leading-loose text-ink-900 sm:text-2xl">
        தெய்வத்தான் ஆகா தெனினும் முயற்சிதன்<br />மெய்வருத்தக் கூலி தரும்
      </p>
      <p className="relative mt-5 text-xs tracking-wide text-ink-400">Thirukkural 619</p>
      <p className="relative mx-auto mt-2 max-w-md text-sm italic text-ink-500">
        Even when fate stands against you, sincere effort will still bring its reward.
      </p>
    </motion.footer>
  )
}
