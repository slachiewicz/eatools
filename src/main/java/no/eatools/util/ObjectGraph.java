package no.eatools.util;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sparx.Collection;

/**
 * Visualize Java Objects as a dot-graph
 *
 * @author ohs
 */
public class ObjectGraph {
    private static final transient Logger LOG = LoggerFactory.getLogger(ObjectGraph.class);

    private final static Set<Class> terminatingClasses = new HashSet<Class>();
    static {
        terminatingClasses.add(String.class);
        terminatingClasses.add(Integer.class);
        terminatingClasses.add(Long.class);
        terminatingClasses.add(Float.class);
        terminatingClasses.add(Double.class);
        terminatingClasses.add(Short.class);
        terminatingClasses.add(Byte.class);
    }

    Set<Object> dependents = new HashSet<Object>();

    public String createDotGraph(final Object obj) {
        final StringBuilder sb = new StringBuilder();
        recurseTheGraph(sb, obj);


        return sb.toString();
    }

    private void recurseTheGraph(final StringBuilder sb, final Object obj) {
        if (obj == null || dependents.contains(obj)) {
            sb.append("\n");
            return;
        }
        final Class clazz = obj.getClass();
        dependents.add(obj);
        sb.append(obj.toString()).append("\n");
        if(terminatingClasses.contains(clazz)) {
            return;
        }
        if(clazz == org.sparx.Collection.class) {
            final org.sparx.Collection collection = (Collection) obj;
            for (final Object aCollection : collection) {
                recurseTheGraph(sb, aCollection);
            }
        }
        sb.append("  ");
        for (final Method method : clazz.getMethods()) {
            final String methodName = method.getName();
            if (methodName.startsWith("Get")
//                    || methodName.startsWith("get")
                    && method.getParameterTypes().length == 0) {
                LOG.debug("Trying " + methodName);
                Object prop = null;
                try {
                    prop = method.invoke(obj, new Object[]{});
                } catch (final Exception e) {
                    LOG.debug("Skipped " + methodName);
                }
                sb.append(methodName).append("=");
                recurseTheGraph(sb, prop);
            }
        }
    }
}
