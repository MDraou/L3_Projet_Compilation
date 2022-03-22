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
        this.tableLocaleCourante = new Ts();
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
        boolean parametresIsNull = false;
        this.tableLocaleCourante = new Ts();
        this.context = Context.PARAM;
        if(node.getParametres() != null) node.getParametres().accept(this);
        else parametresIsNull = true;
        this.context = Context.LOCAL;
        if(node.getVariable() != null) node.getVariable().accept(this);

        if (tableGlobale.getFct(node.getNom()) == null) {
            if (parametresIsNull) node.tsItem = tableGlobale.addFct(node.getNom(), 0, tableLocaleCourante, node);
            else
                node.tsItem = tableGlobale.addFct(node.getNom(), node.getParametres().length(), tableLocaleCourante, node);
        }
        else {
            System.out.println("La fonction " + node.getNom() + " est déjà déclarée autre part");
            System.exit(2);
        }
        if(node.getCorps() != null) node.getCorps().accept(this);
        this.context = Context.GLOBAL;
        tableLocaleCourante = null;
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
        if (tableGlobale.getFct(node.getNom()) != null){
            if (tableGlobale.getFct("main") != null) {
                if (tableGlobale.getFct(node.getNom()).saDecFonc.getParametres() != null) {
                    if (node.getArguments().length() == tableGlobale.getFct(node.getNom()).saDecFonc.getParametres().length()) {
                        node.tsItem = tableGlobale.getFct(node.getNom());
                    } else {
                        System.out.println("La fonction" + node.getNom() + "n'as pas le bon nombre d'arguments");
                        System.exit(2);
                    }
                } else {
                    if (node.getArguments().length() == 0) {
                        node.tsItem = tableGlobale.getFct(node.getNom());
                    } else {
                        System.out.println("La fonction" + node.getNom() + "n'as pas le bon nombre d'arguments");
                        System.exit(2);
                    }
                }
            } else {
                System.out.println("La fonction main n'est pas déclarée");
                System.exit(2);
            }
        }
        else {
            System.out.println("La fonction " + node.getNom() + " n'est pas déclaré");
            System.exit(2);
        }
        defaultOut(node);
        return null;
    }

    public Ts getTableGlobale() {
        return tableGlobale;
    }
}
