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

/**
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
    public static List getCsvHeader(CSVReader reader, Integer rowCount) {
        if(rowCount == 1) {
            return reader.readNext() as List;
        }
        else {
            def headerRows = []
            def size = null

            //read header lines into a list of arrays, check size of each rows
            for(i in 0..rowCount-1) {
                headerRows[i] = reader.readNext();

                log.info "$i. header row size: ${headerRows[i].size()}"

                if(size) {
                    assert headerRows[i].size() == size, "All header rows must have the same size"
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
     * @param self
     * @param cl
     */
    public static void openCsvEachRow(File self, Closure cl) {
        openCsvEachRow(self,0,cl)
    }


    /**
     *
     * @param self
     * @param headerRowCount
     * @param cl
     */
    public static void openCsvEachRow(File self, Integer headerRowCount, Closure cl) {
        CSVReader reader = new CSVReader(new FileReader(self));
        String[] nextLine;

        log.info "headerRowCount '$headerRowCount'"

        //CSV has no header
        if( !headerRowCount ) {
            while ((nextLine = reader.readNext()) != null) {
                cl(nextLine)
            }
        }
        else {
            List header = getCsvHeader(reader, headerRowCount)
            assert header, "No header in CSV"
            def map = [:]

            while ((nextLine = reader.readNext()) != null) {
                assert header.size() <= nextLine.size(), "Header is longer than data line"

                if (headerRowCount == 1) {
                    header.eachWithIndex { String name, i -> map[name] = nextLine[i] }
                }
                else {
                    header.eachWithIndex { List names, i ->
                        getNameMap(map, names, nextLine[i] )
                    }
                }
                
                log.info "map passed to Closure: $map"

                cl(map)
            }
        }
    }

    /**
     *
     * @param map
     * @param l
     * @param val
     * @return
     */
    private static Map getNameMap(Map map, List l, val) {
        def name = l.head()
        def tail = l.tail()

        if(tail) {
            if(!map[name]) { map[name] = [:] }
            map[name] << getNameMap(map[name], tail, val)
        }
        else {
            map[name] = val
        }
        return map
    }
}