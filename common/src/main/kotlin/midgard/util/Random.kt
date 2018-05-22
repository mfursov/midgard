package midgard.util

import java.util.Random

/** Random number generator abstraction. */
class RandomGenerator(seed: Long) {
    val rnd = Random(seed)
}