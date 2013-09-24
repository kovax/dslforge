package org.beedom.dslforge.test.decorators

import groovy.util.logging.Slf4j;

@Slf4j
class ShallCategory {

	def static shall(Object self, String k, Object value) {
		k=k.toLowerCase()

        log.info "ShallCategory.shall ${self}, ${k}, ${value}"

		if (k=="be equal" || k=="be") {
			assert self == value
		}
		else {
		}
	}
}
