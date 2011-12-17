/*
* Copyright 2003-2011 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.beedom.dslforge.integrations

import au.com.bytecode.opencsv.CSVReader
import groovy.util.logging.Slf4j
import au.com.bytecode.opencsv.CSVParser
import org.apache.tools.ant.types.resources.selectors.InstanceOf

/**
 *
 * @author zs.myth
 */
@Slf4j
class OpenCSVCategory {

	/**
	 * 
	 * @param reader
	 * @param rowCount
	 * @param skipLeftCols
	 * @param skipRightCols
	 * @param trim
	 * @return
	 */
    public static List getCsvHeader(CSVReader reader, int rowCount, int skipLeftCols, int skipRightCols, boolean trim) {
        assert rowCount, "row count for header must be grater than zero"

        log.debug "rowCount: $rowCount, skipLeftCols: $skipLeftCols, skipRightCols: $skipRightCols"

        def headerRows = []
        def size = null

        //read header lines into a list of arrays, check size of each rows
        for (i in 0..rowCount-1) {
            headerRows[i] = reader.readNext();

            assert headerRows[i], "$i. row in header is null or zero size"

            log.debug "headerRows[$i] size: ${headerRows[i].size()}"

            //compare current size with the previous
            if (size) {
                assert headerRows[i].size() == size, "$i. header row size is not equal with the previous. All header rows must have the same size"
            }
            size = headerRows[i].size()
        }

        def currentNames = []
        def header = []

        //construct the path of each header, make sure to skip columsn if needed
        for (i in skipLeftCols..size-(1+skipRightCols)) {
            List aList = []
            for (j in 0..rowCount-1) {

                def name = headerRows[j][i]

                //if not null/empty take this string otherwise use the buffer of currentNames
                if (name) { currentNames[j] = trim && name instanceof String ? name.trim() : name }

                name = currentNames[j]

                //if not null/empty append it to the list
                if (name) { aList << name }
            }
            log.info "header[${i-skipLeftCols}] = $aList"
            header << aList
        }
        return header
    }


    /**
     * Recursively process the list of names to build the nested Maps and Lists
     *
     * @param map
     * @param names
     * @param value Can have various types
     * @return
     */
    public static void convertNamesToMaps(Map map, List names, boolean trim, value) {
        String name = names.head()
        List namesTail = names.tail()
        int index = -1

        //Dealing with repeating section, so handle it as List of Maps
        if(name.contains('[') && name.endsWith(']')) {
            int i = name.indexOf('[')
            index = name.substring(i+1,name.size()-1) as int
            name = name.substring(0, i)
        }

        log.debug "$name index:$index names:$names value:'$value'"

        if(namesTail) {
            if(index == -1) {
                if(!map[name]) { map[name] = [:] } //init Map

                convertNamesToMaps(map[name], namesTail, trim, value)
            }
            else {
                //Dealing with repeating section, so handle it as List of Maps
                if(!map[name]) { map[name] = [] } //init List
                if(!map[name][index]) { map[name][index] = [:] } //init Map in the List

                convertNamesToMaps(map[name][index], namesTail, trim, value)
            }
        }
        else {
            map[name] = trim && value && (value instanceof String) ? value.trim() : value
        }
    }

	
    /**
     *
     * @param reader
     * @param options
     * @param cl
     */
    private static void processCsvEachRow(CSVReader reader, Map options, Closure cl) {
        String[] nextLine;

        int headerRows    = options.headerRows
        int skipLeftCols  = options.skipLeftCols
        int skipRightCols = options.skipRightCols
        List header       = options.header

        //CSV has no header
        if(!headerRows && !header) {
            log.warn "No header was specified so reverting to original openCsv behaviour"
            //TODO: processing lines could be done in parallel, but be careful as closure written by user
            while ((nextLine = reader.readNext()) != null) {
                cl(nextLine)
            }
        }
        else {
            if(!header) {
                header = getCsvHeader(reader, headerRows, skipLeftCols, skipRightCols, options.trimHeader)
            }
            else {
                log.debug "external header: $header"
            }

            assert header, "no header is availalle"

            def map = [:]
            def index = 0

            //TODO: processing lines could be done in parallel, but be careful as closure written by user
            while ((nextLine = reader.readNext()) != null) {
                assert header.size() == nextLine.size()-(skipLeftCols+skipRightCols), "Header size must be equal with the size of data line"

                header.eachWithIndex { List names, i ->
                    convertNamesToMaps(map, names, options.trimData, nextLine[i+skipLeftCols])
                }

                log.info "map given to closure of user: $map"

                cl(map,index++)
            }
        }
    }


    /**
     *
     * @param options
     * @return
     */
    public static def setDefaultOptions(Map options) {
        assert options != null, "options cannot be null"

        options.headerRows    = options.headerRows    ?: 0 //Elvis operator
        options.skipLeftCols  = options.skipLeftCols  ?: 0
        options.skipRightCols = options.skipRightCols ?: 0
        options.trimHeader    = options.trimHeader    ?: true
        options.trimData      = options.trimData      ?: false
        options.skipRows      = options.skipRows      ?: 0
        options.separatorChar = options.separatorChar ?: CSVParser.DEFAULT_SEPARATOR
        options.quoteChar     = options.quoteChar     ?: CSVParser.DEFAULT_QUOTE_CHARACTER
        options.escapeChar    = options.escapeChar    ?: CSVParser.DEFAULT_ESCAPE_CHARACTER
        options.strictQuotes  = options.strictQuotes  ?: CSVParser.DEFAULT_STRICT_QUOTES
    }


    /**
     *
     * @param self
     * @return
     */
    public static List openCsvHeader(File self) {
        openCsvHeader(self,[:])
    }


    /**
     *
     * @param self
     * @param options
     * @return
     */
    public static List openCsvHeader(File self, Map options) {
        setDefaultOptions(options)

        CSVReader reader = new CSVReader(
                new FileReader(self), options.separatorChar as char, options.quoteChar as char, options.escapeChar as char, options.skipRows, options.strictQuotes);

        return getCsvHeader(reader, options.headerRows, options.skipLeftCols, options.skipRightCols, options.trimHeader)
    }


    /**
     * Category method
     * 
     * @param self
     * @param cl
     */
    public static void openCsvEachRow(File self, Closure cl) {
        openCsvEachRow(self, [:], cl)
    }


    /**
     * Category method
     *
     * @param self
     * @param options
     * @param cl
     */
    public static void openCsvEachRow(File self, Map options, Closure cl) {
        setDefaultOptions(options)

        CSVReader reader = new CSVReader(
                new FileReader(self), options.separatorChar as char, options.quoteChar as char, options.escapeChar as char, options.skipRows, options.strictQuotes);

        processCsvEachRow(reader,options,cl)
    }


    /**
     * Category method
     *
     * @param self
     * @return
     */
    public static List openCsvHeader(String self) {
        openCsvHeader(self,[:])
    }


    /**
     * Category method
     *
     * @param self
     * @param options
     * @return
     */
    public static List openCsvHeader(String self, Map options) {
        setDefaultOptions(options)

        CSVReader reader = new CSVReader(
                new StringReader(self), options.separatorChar as char, options.quoteChar as char, options.escapeChar as char, options.skipRows, options.strictQuotes);

        return getCsvHeader(reader, options.headerRows, options.skipLeftCols, options.skipRightCols, options.trimHeader)
    }


    /**
     * Category method
     *
     * @param self
     * @param cl
     */
    public static void openCsvEachRow(String self, Closure cl) {
        openCsvEachRow(self, [:], cl)
    }


    /**
     * Category method
     *
     * @param self
     * @param options
     * @param cl
     */
    public static void openCsvEachRow(String self, Map options, Closure cl) {
        setDefaultOptions(options)

        CSVReader reader = new CSVReader(
                new StringReader(self), options.separatorChar as char, options.quoteChar as char, options.escapeChar as char, options.skipRows, options.strictQuotes);

        processCsvEachRow(reader, options, cl)
    }
}
