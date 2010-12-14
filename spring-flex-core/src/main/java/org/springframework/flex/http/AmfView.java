package org.springframework.flex.http;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.view.AbstractView;

import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.AmfMessageSerializer;
import flex.messaging.io.amf.AmfTrace;


public class AmfView extends AbstractView {

    public static final String DEFAULT_CONTENT_TYPE = "application/x-amf";

    private static final Log log = LogFactory.getLog(AmfView.class);
    
    private Set<String> renderedAttributes;

    private boolean disableCaching = true;
    
    public AmfView() {
        setContentType(DEFAULT_CONTENT_TYPE);
    }
    
    /**
     * Returns the attributes in the model that should be rendered by this view.
     */
    public Set<String> getRenderedAttributes() {
        return renderedAttributes;
    }

    /**
     * Sets the attributes in the model that should be rendered by this view. When set, all other model attributes will be
     * ignored.
     */
    public void setRenderedAttributes(Set<String> renderedAttributes) {
        this.renderedAttributes = renderedAttributes;
    }

    /**
     * Disables caching of the generated AMF response.
     *
     * <p>Default is {@code true}, which will prevent the client from caching the generated AMF response.
     */
    public void setDisableCaching(boolean disableCaching) {
        this.disableCaching = disableCaching;
    }
    
    @Override
    protected void prepareResponse(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType(getContentType());
        if (disableCaching) {
            response.addHeader("Pragma", "no-cache");
            response.addHeader("Cache-Control", "no-cache, no-store, max-age=0");
            response.addDateHeader("Expires", 1L);
        }
    }
    
    @Override
    protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Object value = filterModel(model);
        
        AmfMessageSerializer serializer = new AmfMessageSerializer();

        AmfTrace trace = null;
        if (log.isDebugEnabled()) {
            trace = new AmfTrace();
        }
        
        serializer.initialize(new SerializationContext(), response.getOutputStream(), trace);
        serializer.writeObject(value);
        
        if (log.isDebugEnabled()) {
            log.debug("Wrote AMF message:\n" + trace);
        }
    }
    
    /**
     * Filters out undesired attributes from the given model. The return value can be either another {@link Map}, or a
     * single value object.
     *
     * <p>Default implementation removes {@link BindingResult} instances and entries not included in the {@link
     * #setRenderedAttributes(Set) renderedAttributes} property.
     *
     * @param model the model, as passed on to {@link #renderMergedOutputModel}
     * @return the object to be rendered
     */
    protected Object filterModel(Map<String, Object> model) {
        Map<String, Object> result = new HashMap<String, Object>(model.size());
        Set<String> renderedAttributes =
                !CollectionUtils.isEmpty(this.renderedAttributes) ? this.renderedAttributes : model.keySet();
        for (Map.Entry<String, Object> entry : model.entrySet()) {
            if (!(entry.getValue() instanceof BindingResult) && renderedAttributes.contains(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
}