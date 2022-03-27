import java.util.*;

import c3a.*;
import ts.*;
import sa.*;

public class Sa2c3a extends SaDepthFirstVisitor <C3aOperand> {
    private C3a c3a;
    int indentation;
    public C3a getC3a(){return this.c3a;}
    
    public Sa2c3a(SaNode root, Ts tableGlobale){
	c3a = new C3a();
	C3aTemp result = c3a.newTemp();
	C3aFunction fct = new C3aFunction(tableGlobale.getFct("main"));
	c3a.ajouteInst(new C3aInstCall(fct, result, ""));
	c3a.ajouteInst(new C3aInstStop(result, ""));
	indentation = 0;
	root.accept(this);
    }

    public void defaultIn(SaNode node)
    {
	for(int i = 0; i < indentation; i++){System.out.print(" ");}
	indentation++;
	System.out.println("<" + node.getClass().getSimpleName() + ">");
    }

	@Override
	public C3aOperand visit(SaDecTab node){
		defaultIn(node);
		C3aOperand result = new C3aVar(node.tsItem, new C3aConstant(node.getTaille()));
		defaultOut(node);
		return result;
	}

	//peut-etre SaExp à faire

	@Override
	public C3aOperand visit(SaExpInt node) {
		defaultIn(node);
		C3aOperand result = new C3aConstant (node.getVal());
		defaultOut(node);
		return result;
	}

	@Override
	public C3aOperand visit(SaExpVar node) {
		defaultIn(node);
		C3aOperand result = node.getVar().accept(this);
		defaultOut(node);
		return result;
	}

	@Override
	public C3aOperand visit (SaInstEcriture node) {
		defaultIn(node);
		C3aOperand result = c3a.newTemp();
		c3a.ajouteInst(new C3aInstWrite(node.getArg().accept(this), ""));
		defaultOut(node);
		return result;
	}

	@Override
	public C3aOperand visit(SaExpAdd node)
	{
		defaultIn(node);
		C3aOperand op1 = node.getOp1().accept(this);
		C3aOperand op2 = node.getOp2().accept(this);
		C3aOperand result = c3a.newTemp();
		c3a.ajouteInst(new C3aInstAdd(op1, op2, result, ""));
		defaultOut(node);
		return result;
	}

	@Override
	public C3aOperand visit(SaExpSub node)
	{
		defaultIn(node);
		C3aOperand op1 = node.getOp1().accept(this);
		C3aOperand op2 = node.getOp2().accept(this);
		C3aOperand result = c3a.newTemp();
		c3a.ajouteInst(new C3aInstSub(op1, op2, result, ""));
		defaultOut(node);
		return result;
	}

	@Override
	public C3aOperand visit(SaExpMult node)
	{
		defaultIn(node);
		C3aOperand op1 = node.getOp1().accept(this);
		C3aOperand op2 = node.getOp2().accept(this);
		C3aOperand result = c3a.newTemp();
		c3a.ajouteInst(new C3aInstMult(op1, op2, result, ""));
		defaultOut(node);
		return result;
	}

	@Override
	public C3aOperand visit(SaExpDiv node)
	{
		defaultIn(node);
		C3aOperand op1 = node.getOp1().accept(this);
		C3aOperand op2 = node.getOp2().accept(this);
		C3aOperand result = c3a.newTemp();
		c3a.ajouteInst(new C3aInstDiv(op1, op2, result, ""));
		defaultOut(node);
		return result;
	}



    public void defaultOut(SaNode node)
    {
	indentation--;
		for(int i = 0; i < indentation; i++){System.out.print(" ");}
		System.out.println("</" + node.getClass().getSimpleName() + ">");
    }
    
    
}
