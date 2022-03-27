import sa.*;
import ts.Ts;

public class Sa2ts extends SaDepthFirstVisitor <Void> {

    enum Context {
	LOCAL,
	GLOBAL,
	PARAM
    }
    
    private Ts tableGlobale;
    private Ts tableLocaleCourante;
    private Context context;
    private final int sizeInt = 4;

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
        if (!this.tableGlobale.variables.containsKey(node.getNom()) && context == Context.GLOBAL) {
            node.tsItem = tableGlobale.addVar(node.getNom(), sizeInt);
        }
        else if (!this.tableLocaleCourante.variables.containsKey(node.getNom()) && context == Context.PARAM) {
            node.tsItem = tableLocaleCourante.addParam(node.getNom());
        }
        else if (!this.tableLocaleCourante.variables.containsKey(node.getNom()) && context == Context.LOCAL) {
            node.tsItem = tableLocaleCourante.addVar(node.getNom(), sizeInt);
        }
        defaultOut(node);
        return null;
    }

    // DEC -> var id taille
    @Override
    public Void visit(SaDecTab node){
        defaultIn(node);
        if (!this.tableGlobale.variables.containsKey(node.getNom()) && this.context == Context.GLOBAL) {
            node.tsItem = this.tableGlobale.addVar(node.getNom(), sizeInt * node.getTaille());
        }
        else {
            System.out.println("un tableau doit etre une variable global");
            System.exit(1);
        }
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
            System.exit(1);
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
        Ts table = null;
        if (context == Context.GLOBAL){
            if (this.tableGlobale.variables.containsKey(node.getNom())){
                table = tableGlobale;
            }
            else {
                System.out.println("0 il existe pas de variable global " + node.getNom());
                System.exit(1);
            }
        }
        else {
            if (this.tableLocaleCourante.variables.containsKey(node.getNom())){
                table = tableLocaleCourante;
            }
            else if (this.tableGlobale.variables.containsKey(node.getNom())){
                table = tableGlobale;
            }
            else {
                System.out.println("1 il existe pas de variable global " + node.getNom());
                System.exit(1);
            }
        }
        if (table.getVar(node.getNom()).getTaille() != this.sizeInt){
            System.out.println("un entier ne peut pas etre indicée ");
            System.exit(1);
        }
        node.tsItem = table.getVar(node.getNom());
        defaultOut(node);
        return null;
    }

    @Override
    public Void visit(SaVarIndicee node)
    {
        defaultIn(node);
        if (node.getIndice() != null) {
            if(tableGlobale.getVar(node.getNom()) != null){
                node.getIndice().accept(this);
                node.tsItem = tableGlobale.getVar(node.getNom());
            }
            else {
                System.out.println("2 il existe pas de variable " + node.getNom());
                System.exit(1);
            }
        }
        else {
            System.out.println("un tableau ne doit pas etre utilisé sans indice");
            System.exit(1);
        }
        defaultOut(node);
        return null;
    }

    @Override
    public Void visit(SaAppel node)
    {
        defaultIn(node);
        if(context != Context.LOCAL){
            System.out.println("la fonction doit etre appeler dans un context local");
            System.exit(1);
        }
        if (tableGlobale.fonctions.containsKey(node.getNom())){
            int nbArgument = 0;
            if (node.getArguments() != null){
                nbArgument = node.getArguments().length();
            }
            if (tableGlobale.getFct(node.getNom()).nbArgs == nbArgument){
                node.tsItem = tableGlobale.getFct(node.getNom());
                if(node.getArguments() != null) node.getArguments().accept(this);
            }
            else {
                System.out.println("La fonction " + node.getNom() + " n'as pas le bon nombre d'arguments");
                System.exit(1);
            }
        }
        else {
            System.out.println("La fonction " + node.getNom() + " n'est pas déclaré");
            System.exit(1);
        }
        defaultOut(node);
        return null;
    }

    public Ts getTableGlobale() {
        return tableGlobale;
    }
}
