package org.beedom.dslforge.test.decorators

import java.util.logging.Logger;

class ShallCategory {

    protected static Logger log = Logger.getLogger(ShallCategory.class.getName());
    
	def static shall(Object self, String k, Object value) {
		k=k.toLowerCase()

        log.info("ShallCategory.shall ${self}, ${k}, ${value}")

		if (k=="be equal" || k=="be") {
			assert self == value
		}
		else {
		}
	}
}
