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

import java.util.Map;

import groovy.lang.Closure;
import groovy.util.logging.Slf4j
import au.com.bytecode.opencsv.CSVReader

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * 
 * @author zs.myth
 */
@Slf4j
class CompareCSV {

    /**
     * DSL Key for DSLEngine
     */
    def static final dslKey = "csvCompare"

    private boolean failFast = false;

    def actualDatasource  = [:]
    def expectedDatasource = [:]    
    def difference = []

    def index = 0

    /**
     * 
     */
    private void setCSVReader(ds) {
        OpenCSVCategory.setDefaultOptions(ds.options)

        ds.reader = new CSVReader(
            new FileReader(ds.file),
            ds.options.separatorChar as char,
            ds.options.quoteChar as char,
            ds.options.escapeChar as char,
            ds.options.skipRows,
            ds.options.strictQuotes
        );
    }

    /**
     * 
     * @param ds
     */
    private void setHeader(ds) {
        if(!ds.reader) {
            setCSVReader(ds)
        }

        if(!ds.header) {
            assert ds.options.headerRows, "No headerRows was specified"

            ds.header = OpenCSVCategory.getCsvHeader(
                                ds.reader, ds.options.headerRows,
                                ds.options.skipLeftCols, ds.options.skipRightCols,
                                ds.options.trimHeader)
        }
        else {
            log.debug "using external header: ${ds.header}"
        }
    }

    /**
     * 
     * @param ds
     * @return
     */
    private def getNextRow(ds) {
        String[] nextLine;
        def map = [:]

        if(!ds.header) {
            setHeader(ds)
        }

        nextLine = ds.reader.readNext()

        if (nextLine) {
            assert ds.header.size() == nextLine.size()-(ds.options.skipLeftCols+ds.options.skipRightCols), "Header size must be equal with the size of data line"

            ds.header.eachWithIndex {
                List names, i ->
                OpenCSVCategory.convertNamesToMaps(map, names, ds.options.trimData, nextLine[i+ds.options.skipLeftCols])
            }

            log.info "map for closure: $map"

            return map
        }

        return null
    }


    /**
     * 
     * @param cl
     */
    public void map(Closure cl) {
        int i = 0;
        def actualNext;

        log.info "actualDS   : $actualDatasource"
        log.info "expectedDS : $expectedDatasource"

        //TODO: cover cases when files have different number of rows
        while ((actualNext = getNextRow(actualDatasource)) != null) {
            cl (actualNext, getNextRow(expectedDatasource), index++)
        }
        
        log.debug "$difference"
    }

    /**
     * 
     * @param L
     * @return
     */
    def assertThat(actual) {
        return [equalTo: { expected ->
            try {
                assertThat(actual, equalTo(expected))
            }
            catch (AssertionError e) {
                if(failFast) {
                    throw e
                }
                else {
                    log.debug "${e.message}"
                    difference << [index: index, actual: actual, expected: expected]
                }
            }
        }]
    }
}
