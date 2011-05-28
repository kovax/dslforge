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
     * @return
     */
    private static List getCsvHeader(CSVReader reader, Integer rowCount) {
        assert rowCount, "row count for header must be grater than zero"

        log.info "headerRows '$rowCount'"

        if(rowCount == 1) {
            return reader.readNext() as List;
        }
        else {
            def headerRows = []
            def size = null

            //read header lines into a list of arrays, check size of each rows
            for(i in 0..rowCount-1) {
                headerRows[i] = reader.readNext();

                assert headerRows[i], "$i. row in header is null or zero size"

                log.info "$i. header row size: ${headerRows[i].size()}"

                //compare current size with the previous
                if(size) {
                    assert headerRows[i].size() == size, "$i. header row size is not equal with the previous. All header rows must have the same size"
                }
                size = headerRows[i].size()
            }

            def currentNames = []
            def header = []

            //construct the path of each header
            for(i in 0..size-1) {
                header[i] = []
                for(j in 0..rowCount-1) {
                    def name = headerRows[j][i]

                    //if not null/empty take this string otherwise use the buffer of currentNames
                    if(name) { currentNames[j] = name }
                    else { name = currentNames[j] }

                    //if not null/empty append it to the list
                    if(name) { header[i] << name }
                }
                log.info "$i. header = ${header[i]}"
            }
            return header
        }
    }


    /**
     *
     * @param options
     * @return
     */
    private static def setDefaultOptions(Map options) {
        assert options != null, "option cannot be null"

        options.headerRows    = options.headerRows    ?: 0 //Elvis operator
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
        getCsvHeader(self,[:])
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
                new FileReader(self), options.separatorChar, options.quoteChar, options.escapeChar, options.skipRows, options.strictQuotes);

        return getCsvHeader(reader, options.headerRows)
    }


    /**
     *
     * @param self
     * @param cl
     */
    public static void openCsvEachRow(File self, Closure cl) {
        openCsvEachRow(self, [:], cl)
    }


    /**
     *
     * @param self
     * @param headerRowCount
     * @param cl
     */
    public static void openCsvEachRow(File self, Map options, Closure cl) {
        setDefaultOptions(options)

        String[] nextLine;
        CSVReader reader = new CSVReader(
                new FileReader(self), options.separatorChar, options.quoteChar, options.escapeChar, options.skipRows, options.strictQuotes);

        def headerRows = options.headerRows
        List header = options.header

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
                header = getCsvHeader(reader, headerRows)
            }
            else {
                log.info "external header: $header"
                headerRows = header.size()
            }

            assert header, "no header is availbale"
            def map = [:]

            //TODO: processing lines could be done in parallel, but be careful as closure written by user
            while ((nextLine = reader.readNext()) != null) {
                assert header.size() == nextLine.size(), "Header size must be equal with the size of data line"

                if (headerRows == 1) {
                    header.eachWithIndex { String name, i ->
                        map[name] = nextLine[i]
                    }
                }
                else {
                    header.eachWithIndex { List names, i ->
                        convertNamesToMaps(map, names, nextLine[i] )
                    }
                }
                
                log.info "map to closure: $map"

                cl(map)
            }
        }
    }

    /**
     * Recursively process the list of names to build the Maps and Lists which will be passed to the Closure
     *
     * @param map
     * @param names
     * @param value
     * @return
     */
    private static void convertNamesToMaps(Map map, List names, value) {
        String name = names.head()
        List namesTail = names.tail()
        int index = -1

        //Dealing with repeating section, so handle it as List of Maps
        if(name.contains('[') && name.endsWith(']')) {
            int i = name.indexOf('[')
            index = name.substring(i+1,name.size()-1) as int
            name = name.substring(0, i)
        }

        log.info "$name index:$index names:$names value:$value"

        if(namesTail) {
            if(index == -1) {
                if(!map[name]) { map[name] = [:] } //init Map

                convertNamesToMaps(map[name], namesTail, value)
            }
            else {
                //Dealing with repeating section, so handle it as List of Maps
                if(!map[name]) { map[name] = [] } //init List
                if(!map[name][index]) { map[name][index] = [:] } //init Map in the List

                convertNamesToMaps(map[name][index], namesTail, value)
            }
        }
        else {
            map[name] = value
        }
    }
}
