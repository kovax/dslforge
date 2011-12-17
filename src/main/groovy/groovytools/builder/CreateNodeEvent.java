package groovytools.builder;

public class CreateNodeEvent {
    protected String name;
    protected Object parent;
    protected Object node;

    public CreateNodeEvent(String name, Object node, Object parent) {
        this.name = name;
        this.node = node;
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getNode() {
        return node;
    }

    public void setNode(Object node) {
        this.node = node;
    }

    public Object getParent() {
        return parent;
    }

    public void setParent(Object parent) {
        this.parent = parent;
    }

    public boolean getIsRoot() {
        return parent == null;
    }
}
