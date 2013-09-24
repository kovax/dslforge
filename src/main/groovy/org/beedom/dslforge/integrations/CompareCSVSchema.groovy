package org.beedom.dslforge.integrations

mbObjectKeys = [(org.beedom.dslforge.integrations.CompareField) : 'name']

def compareFieldSchema = metaBuilder.define {
	field(factory: org.beedom.dslforge.integrations.CompareField) {
		properties {
			name()
			expectedName()
			actualName()
			type()
			isKey()
			isIgnored()
		}
	}
}
