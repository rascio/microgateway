package it.r.microgateway.server.url;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.r.ports.api.BinaryContent;
import it.r.ports.api.Message;
import it.r.ports.utils.BeanUtils;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

class SpringPostRequestMapping<R extends Message<?>> extends SpringMessageMapping<R>{

    private static final Logger logger = LoggerFactory.getLogger(SpringPostRequestMapping.class);

    private final DiskFileItemFactory factory;
    private final ObjectMapper mapper;

    SpringPostRequestMapping(String domain, String template, Class<R> type, DiskFileItemFactory factory, ObjectMapper mapper) {
        super(domain, template, type);
        this.factory = factory;
        this.mapper = mapper;
    }

    @Override
    public String link(Object api) {
        return uri(new BeanMap(api));
    }

    @Override
    public R createMessage(HttpServletRequest request) {
        final Map<String, String> pathParams = extractPathParams(request.getRequestURI());
        final R api = ServletFileUpload.isMultipartContent(request)
            ? createFromMultipart(type, request)
            : createWithJson(type, request);

        return BeanUtils.populate(api, pathParams);
    }

    private R createFromMultipart(Class<R> type, HttpServletRequest request) {
        final R api = BeanUtils.newInstance(type);
        final ServletFileUpload fileUpload = new ServletFileUpload(this.factory);
        final List<FileItem> items = parseRequest(fileUpload, request);
        logger.debug("fields: {}", items);

        items.forEach(item -> {
            logger.debug("reading {} isFormField:{}", item.getFieldName(), item.isFormField());
            if (item.isFormField()) {
                BeanUtils.setProperty(api, item.getFieldName(), item.getString());
            } else {
                try {
                    BeanUtils.setProperty(api, item.getFieldName(), new BinaryContent(item.get(), item.getContentType()));
                } catch (Exception e) {
                    final Field error = FieldUtils.getField(api.getClass(), item.getFieldName());
                    throw new RuntimeException("Field " + error.getName() + " is of type " + error.getType().getName() + ". But was sent as a Binary.");
                }
            }
        });
        logger.debug("Created with multipart: {}", api);
        return api;
    }

    private static List<FileItem> parseRequest(ServletFileUpload sfu, HttpServletRequest request) {
        try {
            return sfu.parseRequest(request);
        } catch (FileUploadException e) {
            throw new RuntimeException("Error parsing request", e);
        }
    }

    private <T> T read(InputStream stream, Class<T> type) {
        String rawJson = null;
        try {
            rawJson = IOUtils.toString(stream);
        }
        catch (IOException e) {
            throw new RuntimeException("Error while parsing stream [" + rawJson + "] for command " + type, e);
        }
        return fromJson(mapper, rawJson, type);
    }

    private R createWithJson(Class<R> type, HttpServletRequest httpRequest) {
        try {
            final InputStream body = httpRequest.getInputStream();
            if (body.available() > 0) {
                final R request = read(body, type);
                logger.debug("Created with multipart: {}", request);
                return request;
            }
        }
        catch (IOException e){
            throw new RuntimeException("There was a problem with the stream", e);
        }
        return BeanUtils.newInstance(type);
    }

    private static <T> T fromJson(ObjectMapper mapper, String json, Class<T> type) {
        try {
            return mapper.readValue(json, type);
        }
        catch (IOException e) {
            throw new RuntimeException("Error while parsing json " + json + " to " + type, e);
        }
    }
}
