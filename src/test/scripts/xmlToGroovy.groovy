
import javax.xml.parsers.DocumentBuilderFactory
import org.codehaus.groovy.tools.xml.DomToGroovy

def builder     = DocumentBuilderFactory.newInstance().newDocumentBuilder()
def inputStream = new FileInputStream(new File("src/test/data/soap.xml"))
def document    = builder.parse(inputStream)
def output      = new StringWriter()
def converter   = new DomToGroovy(new PrintWriter(output))

converter.print(document)
println output.toString()