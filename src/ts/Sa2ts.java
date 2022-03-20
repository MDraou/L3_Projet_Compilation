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

}
