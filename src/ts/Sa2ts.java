package ts;
import sa.*;

public class Sa2ts extends SaDepthFirstVisitor <Void> {

    enum Context {
	LOCAL,
	GLOBAL,
	PARAM
    }
    
    private Ts tableGlobale;
    private Ts tableLocaleCourante;
    private Context context;

    public Sa2ts(SaNode node) {
        this.tableGlobale = new Ts();
        this.tableGlobale = new Ts();
        this.context = Context.GLOBAL;
        node.accept(this);
    }

    // DEC -> var id
    @Override
    public Void visit(SaDecVar node)
    {
        defaultIn(node);
        defaultOut(node);
        return null;
    }

    // DEC -> var id taille
    @Override
    public Void visit(SaDecTab node){
        defaultIn(node);
        defaultOut(node);
        return null;
    }

    // DEC -> fct id LDEC LDEC LINST
    @Override
    public Void visit(SaDecFonc node)
    {
        defaultIn(node);
        if(node.getParametres() != null) node.getParametres().accept(this);
        if(node.getVariable() != null) node.getVariable().accept(this);
        if(node.getCorps() != null) node.getCorps().accept(this);
        defaultOut(node);
        return null;
    }

    @Override
    public Void visit(SaVarSimple node)
    {
        defaultIn(node);
        defaultOut(node);
        return null;
    }

    @Override
    public Void visit(SaVarIndicee node)
    {
        defaultIn(node);
        node.getIndice().accept(this);
        defaultOut(node);
        return null;
    }

    @Override
    public Void visit(SaAppel node)
    {
        defaultIn(node);
        if(node.getArguments() != null) node.getArguments().accept(this);
        defaultOut(node);
        return null;
    }

    public Ts getTableGlobale() {
        return tableGlobale;
    }
}
