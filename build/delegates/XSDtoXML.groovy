package org.beedom.dslforge.test.delegates

import groovy.xml.MarkupBuilder

/**
 * Created by IntelliJ IDEA.
 * User: kovax
 * Date: 3/12/11
 * Time: 11:33 AM
 * To change this template use File | Settings | File Templates.
 */
class XSDtoXML {
    def currentNS
    def declaredNameSpaces = [:]
    def schemas = [:]

    def rootXSD
    def rootXSDPath
    def rootNS

    def xmlWriter
    def pathWriter
    def xmlInst

    public XSDtoXML() {
        pathWriter = new StringWriter()

        xmlWriter = new StringWriter()
        xmlInst = new MarkupBuilder(xmlWriter)
    }

    def getSchema(name) {
        def schema = schemas[name]
        assert schema, "Namespace '${name}' is not handled, check import statement in xsd"
        return schema
    }

    def getDefaultValue(type) {
        if (type.contains("string"))
            return ""
        else if (type.contains("int") || type.contains("decimal"))
            return 0
        else if (type.contains("double"))
            return 0.0
        else if (type.contains("boolean"))
            return false

        return "unknownType"
    }

    /**
     * return a Map with namespace and type names
     *
     * @param typeNameWithNS
     * @return
     */
    def getNSandType(typeNameWithNS) {
        def buffer = typeNameWithNS.split(":")
        def ns
        def typeName

        if (buffer.length == 2) {
            ns = buffer[0]
            typeName = buffer[1]
        }
        else {
            ns = currentNS
            typeName = typeNameWithNS
        }

        return ['ns': ns, 'type': typeName]
    }

    /**
     * returns a Map containig the attributes with value
     *
     * @param path
     * @param typeNameWithNS
     * @return
     */
    def processAttributes(path, typeNameWithNS) {
        def typeMap = getNSandType(typeNameWithNS)

        //this is a sequence element with built-in type, therefore has no attributes
        if (typeMap['ns'] == 'xsd')
            return

        def schema = getSchema(typeMap['ns'])
        def node = schema.children().find { it.@name.text().equals(typeMap['type']) }
        def attrsMap = [:]

        node.'xsd:attribute'.breadthFirst().collect { attr ->
            println "attrib  : @${attr.@name}"
            def required = attr.@use == "required"

            def t = processType(path, attr.@type.text())
            def value = getDefaultValue(t)

            if (attr.@fixed.text())
                value = attr.@fixed.text()

            attrsMap.put(attr.@name.text(), value)

            pathWriter << "set " << path << ".(XML.Attribute)" << attr.@name.text() << "=" << value << ";\n"
        }
        return attrsMap
    }

    def processSimpleType(path, node) {
        println "simple  : '${node.@name}'"

        def type = processType(path, node.'xsd:restriction'.@base.text());
        StringBuffer restrict = new StringBuffer()

        node.'xsd:restriction'.breadthFirst().collect {
            println "        : -${it.name()} -> ${it.@value}"

            //TODO: make use this for value generation and value check
            if (it.name() == "enumeration") {
                if (restrict.length() == 0)
                    restrict.append(it.@value.text())
                else
                    restrict.append("," + it.@value.text())
            }
            else if (it.name() == "length") {
            }
            else if (it.name() == "pattern") {
            }
        }

        if (restrict)
            type += ":" + restrict

        return type
    }

    def processComplexType(path, node) {
        println "complex : '${node.@name}'"
        def tempPath = path.toString()

        //deals with sequence-elements only
        def seq = node.'xsd:sequence'.'xsd:element'.collect { i ->
            def seqName = i.@name.text()
            def seqTypeName = i.@type.text()
            def minOccurs = i.@minOccurs.text()
            def maxOccurs = i.@maxOccurs.text()

            println "seqelem : #${seqName}:${minOccurs}->${maxOccurs}"

            if (maxOccurs == 'unbounded' || maxOccurs != '1')
            path << '.' << seqName << '[]'
            else
                path << '.' << seqName

            xmlInst."$seqName"(processAttributes(path, seqTypeName)) {

                def type = processType(path, seqTypeName)
                if (type) {
                    def value = getDefaultValue(type)
                    pathWriter << "set " << tempPath << ".(XML.Element)" << seqName << "=" << value << ";\n"

                    //forces the MarkupBulder to add the text data
                    yield "$value"
                }
            }
            path = new StringBuffer(tempPath)
        }
    }

    def processType(path, typeNameWithNS) {
        //get namespace and type name
        def typeMap = getNSandType(typeNameWithNS)

        if (typeMap['ns'] == 'xsd') {
            println "builtin : '${typeMap['type']}'"
            return typeMap['type']
        }
        else {
            def nsTemp = currentNS
            currentNS = typeMap['ns']
            def schema = getSchema(currentNS)

            //processing type assuming it is a complex or a simple type
            def type = schema.children().find { it.@name.text().equals(typeMap['type']) }
            assert type, "Type '${typeMap['type']}' was not found in schema '${currentNS}'"

            if (type.name() == "simpleType") {
                return processSimpleType(path, type)
            }
            else if (type.name() == "complexType") {
                processComplexType(path, type)
            }
            else {
                //is this case possible???
                assert false, "Type ${typeName} is not a simple or complex type"
            }

            currentNS = nsTemp
        }
    }

    def importSchemas() {
        def schema = getSchema(currentNS)

        def schemaLocations = [:]

        //collects the imported schema locations
        schema.'xsd:import'.breadthFirst().collect { ns ->
            schemaLocations.put(ns.@namespace.text(), rootXSDPath + '/' + ns.@schemaLocation.text())
        }

        //loops the namespaces and parses the imported schemas
        declaredNameSpaces.each { nsName, nsURL ->
            def schemaLoc = schemaLocations[nsURL]
            if (schemaLoc)
                schemas.put(nsName, new XmlSlurper().parse(schemaLoc).declareNamespace(declaredNameSpaces))
        }
    }

    def processRootElement(rootName, ns, targetNS) {
        currentNS = ns
        def schema = getSchema(ns)

        importSchemas()

        def rootType = schema.'xsd:element'.find { it.@name.text().equals(rootName) }

        assert rootType, "Root element '${rootName}' was not found in schema '${ns}'"

        def path = new StringBuffer("LeadingPath." + targetNS + ":" + rootName)

        xmlInst."$targetNS:$rootName"(processAttributes(path, rootType.@type.text())) {
            processType(path, rootType.@type.text())
        }

        println pathWriter.toString()
        println xmlWriter.toString()
    }

    public static void main(String[] args) {
        XSDtoXML g = new XSDtoXML()

//		g.declaredNameSpaces = [
//			tns:'http://bar.types.dbp.hu/BARData',
//			xsd:'http://www.w3.org/2001/XMLSchema'
//		]
//		g.rootXSDPath = './xsd_wsdl/hu/dbp/types/bar'
//		g.rootXSD     = 'BARData.xsd'
//		g.rootNS      = 'tns'
//		g.schemas.put( g.rootNS, new XmlSlurper().parse(g.rootXSDPath+'/'+g.rootXSD).declareNamespace(g.declaredNameSpaces))
//		g.processRootElement( 'getSyncBARResponse', 'tns', 'bar' )

        g.declaredNameSpaces = [
                tns: "http://khr.types.raiffeisen.hu/KHRData",
                common: "http://common.types.raiffeisen.hu/common",
                Ocom: "http://companycommon.companyinfo.types.raiffeisen.hu/CompanyCommon",
                xsd: "http://www.w3.org/2001/XMLSchema"
        ]

        g.rootXSDPath = '../KHRServices_MSet/importFiles/hu/raiffeisen/types/khr'
        g.rootXSD = 'KHRData.xsd'
        g.rootNS = 'tns'

        g.schemas.put(g.rootNS, new XmlSlurper().parse(g.rootXSDPath + '/' + g.rootXSD).declareNamespace(g.declaredNameSpaces))
        g.processRootElement('getDiscoveredPersonFromKHRResponse', 'tns', 'khr')
    }
}
