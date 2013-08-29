package oracle.eclipse.tools.cloud.dev.tasks;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import oracle.eclipse.tools.cloud.dev.tasks.data.CloudDevResults;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.commons.net.Policy;
import org.eclipse.mylyn.commons.net.WebUtil;
import org.eclipse.mylyn.tasks.core.IRepositoryPerson;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.RepositoryResponse.ResponseKind;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentSource;
import org.eclipse.mylyn.tasks.core.data.TaskAttachmentMapper;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMetaData;
import org.eclipse.mylyn.tasks.core.data.TaskCommentMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskOperation;
import org.eclipse.osgi.util.NLS;

import com.tasktop.c2c.server.common.service.AuthenticationException;
import com.tasktop.c2c.server.common.service.ConcurrentUpdateException;
import com.tasktop.c2c.server.common.service.InsufficientPermissionsException;
import com.tasktop.c2c.server.common.service.domain.QueryResult;
import com.tasktop.c2c.server.common.service.domain.Region;
import com.tasktop.c2c.server.common.service.domain.SortInfo;
import com.tasktop.c2c.server.common.service.domain.SortInfo.Order;
import com.tasktop.c2c.server.common.service.domain.criteria.Criteria;
import com.tasktop.c2c.server.common.service.domain.criteria.CriteriaParser;
import com.tasktop.c2c.server.common.service.web.Error;
import com.tasktop.c2c.server.profile.domain.project.Profile;
import com.tasktop.c2c.server.tasks.domain.Attachment;
import com.tasktop.c2c.server.tasks.domain.AttachmentHandle;
import com.tasktop.c2c.server.tasks.domain.Comment;
import com.tasktop.c2c.server.tasks.domain.Component;
import com.tasktop.c2c.server.tasks.domain.FieldDescriptor;
import com.tasktop.c2c.server.tasks.domain.Iteration;
import com.tasktop.c2c.server.tasks.domain.Keyword;
import com.tasktop.c2c.server.tasks.domain.Milestone;
import com.tasktop.c2c.server.tasks.domain.PredefinedTaskQuery;
import com.tasktop.c2c.server.tasks.domain.Priority;
import com.tasktop.c2c.server.tasks.domain.Product;
import com.tasktop.c2c.server.tasks.domain.QuerySpec;
import com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration;
import com.tasktop.c2c.server.tasks.domain.Task;
import com.tasktop.c2c.server.tasks.domain.TaskHandle;
import com.tasktop.c2c.server.tasks.domain.TaskResolution;
import com.tasktop.c2c.server.tasks.domain.TaskSeverity;
import com.tasktop.c2c.server.tasks.domain.TaskStatus;
import com.tasktop.c2c.server.tasks.domain.TaskUserProfile;
import com.tasktop.c2c.server.tasks.service.CriteriaQueryArguments;
import com.tasktop.c2c.server.tasks.service.PredefinedQueryArguments;
import com.tasktop.c2c.server.tasks.service.SaveAttachmentArguments;
import java.io.BufferedReader;
import java.util.logging.Level;
import org.apache.commons.httpclient.HttpStatus;

public class CloudDevClient {
	
	private final AbstractWebLocation location;
	private final TaskRepository taskRepository;
	private final HttpClient httpClient = new HttpClient(WebUtil.getConnectionManager());
	
	private RepositoryConfiguration repositoryConfiguration = null;
	
	private final CloudDevOperation OPERATIONS[] = new CloudDevOperation[] {
			CloudDevOperation.NONE,
			CloudDevOperation.UNCONFIRMED,
			CloudDevOperation.NEW,
			CloudDevOperation.ASSIGNED,
			CloudDevOperation.REOPENED,
			CloudDevOperation.VERIFIED,
			CloudDevOperation.CLOSED,
			CloudDevOperation.DUPLICATE,
			CloudDevOperation.RESOLVED
	};
	private final List<CloudDevOperation> OPERATION_PREDEFINED_ORDER = Arrays.asList(OPERATIONS);


	public CloudDevClient(AbstractWebLocation location, TaskRepository taskRepository) {
		this.location = location;
		this.taskRepository = taskRepository;
		//?
		WebUtil.configureHttpClient(httpClient, "");
	}

	public QueryResult<Task> query(IRepositoryQuery query, IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		
		final String url = query.getUrl();
        final String request;
		
        final Region region = new Region( 0, 32767 );
        final SortInfo sortInfo = new SortInfo( "creationDate", Order.ASCENDING );
        final QuerySpec querySpec = new QuerySpec( region, sortInfo, true );
        final Object queryRequestObject;
        
		if( url.equals( CloudDevConstants.CRITERIA_QUERY ) )
		{
		    final Criteria criteria = CriteriaParser.parse( query.getAttribute( CloudDevConstants.QUERY_CRITERIA ) );
            queryRequestObject = new CriteriaQueryArguments( criteria, querySpec );
		}
		else if( url.equals( CloudDevConstants.PREDEFINED_QUERY ) )
		{
            final String name = query.getAttribute( CloudDevConstants.QUERY_NAME );
            queryRequestObject = new PredefinedQueryArguments( PredefinedTaskQuery.valueOfString( name ), querySpec );
		}
		else
		{
		    throw new IllegalStateException();
		}

        try
        {
            request = ( new ObjectMapper() ).writeValueAsString( queryRequestObject );
        }
        catch( Exception e )
        {
            throw new RuntimeException( e );
        }

		CloudDevResults result = getCloudDevResults(getUri(url), request, monitor);
		
		return result.getQueryResult();
	}
	
	private CloudDevResults getCloudDevResults(final String uri, final String request, IProgressMonitor monitor) throws CoreException {
		try {
			monitor.beginTask(uri, IProgressMonitor.UNKNOWN);
	
			PostMethod method = new PostMethod(uri);
			method.setDoAuthentication(true);
			method.setRequestHeader("Content-Type", "application/json");
			try {
				method.setRequestEntity(new StringRequestEntity(request, "application/json", "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				throw new CoreException(RepositoryStatus.createStatus(taskRepository, IStatus.ERROR, CloudDevTasksConnectorBundle.ID, e.getMessage()));
			}
			try {
				HostConfiguration hostConfiguration = WebUtil.createHostConfiguration(httpClient, location, monitor);
				WebUtil.execute(httpClient, hostConfiguration, method, monitor);
				InputStream in = WebUtil.getResponseBodyAsStream(method, monitor);
				try {
					ObjectMapper m = new ObjectMapper();
					m.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
					CloudDevResults results = m.readValue(new InputStreamReader(in), CloudDevResults.class);
					if (results.getError() != null) {
						throw new CoreException(getStatusFromError(results.getError()));
					}
					return results;
				} finally {
					in.close();
				}
			} catch (IOException e) {
				throw new CoreException(new RepositoryStatus(taskRepository, IStatus.ERROR, CloudDevTasksConnectorBundle.ID, RepositoryStatus.ERROR_IO, e.getMessage(), e));
			} finally {
				method.releaseConnection();
			}
		} finally {
			monitor.done();
		}
	}
	
	private CloudDevResults getCloudDevResults(final String uri, IProgressMonitor monitor) throws CoreException {
		try {
			monitor.beginTask(uri, IProgressMonitor.UNKNOWN);
	
			GetMethod method = new GetMethod(uri);
			method.setDoAuthentication(true);
			try {
				HostConfiguration hostConfiguration = WebUtil.createHostConfiguration(httpClient, location, monitor);
				WebUtil.execute(httpClient, hostConfiguration, method, monitor);
				InputStream in = WebUtil.getResponseBodyAsStream(method, monitor);
				try {
					ObjectMapper m = new ObjectMapper();
					m.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
					CloudDevResults results = m.readValue(new InputStreamReader(in), CloudDevResults.class);
					if (results.getError() != null) {
						throw new CoreException(getStatusFromError(results.getError()));
					}
					return results;
				} finally {
					in.close();
				}
			} catch (IOException e) {
				throw new CoreException(new RepositoryStatus(taskRepository, IStatus.ERROR, CloudDevTasksConnectorBundle.ID, RepositoryStatus.ERROR_IO, e.getMessage(), e));
			} finally {
				method.releaseConnection();
			}
		} finally {
			monitor.done();
		}
	}
	
        // "api/profile"
        private void getProfile(IProgressMonitor monitor) throws CoreException {
		try {
                    
			monitor.beginTask("http://developer.us.oracle.com/qa-dev/api/profile", IProgressMonitor.UNKNOWN);
	
			GetMethod method = new GetMethod("http://developer.us.oracle.com/qa-dev/api/profile");
			method.setDoAuthentication(true);
			try {
				HostConfiguration hostConfiguration = WebUtil.createHostConfiguration(httpClient, location, monitor);
                                int r = WebUtil.execute(httpClient, hostConfiguration, method, monitor);
                                 ProfileWrapper results;
                                if (r == HttpStatus.SC_OK) {
                                    InputStream in = WebUtil.getResponseBodyAsStream(method, monitor);
                                    try {
                                            ObjectMapper m = new ObjectMapper();
                                            m.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                                            InputStreamReader isr = new InputStreamReader(in);
                                           
                                            char[] c = new char[4000];
                                            isr.read(c);
                                            System.out.println(new String(c));
                                            
//                                            Profile p = m.readValue(new InputStreamReader(in), Profile.class);
//
//                                            System.out.println(" ++++ " + p.getUsername());
//                                            System.out.println(" ++++ " + results.profile.getUsername());

                                    } finally {
                                            in.close();
                                    }
                                } else {
                                    InputStream in = WebUtil.getResponseBodyAsStream(method, monitor);
                                    try {
                                        ObjectMapper m = new ObjectMapper();
                                        m.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                                        ErrorWrapper ew = m.readValue(new InputStreamReader(in), ErrorWrapper.class);
                                        throw new IOException(ew.error.getMessage());
                                    } finally {
                                        in.close();
                                    }
                                }
			} catch (IOException e) {
				throw new CoreException(new RepositoryStatus(taskRepository, IStatus.ERROR, CloudDevTasksConnectorBundle.ID, RepositoryStatus.ERROR_IO, e.getMessage(), e));
			} finally {
				method.releaseConnection();
			}
		} finally {
			monitor.done();
		}
	}

	private RepositoryStatus getStatusFromError(Error error) {
		final String code = error.getErrorCode();
		if (ConcurrentUpdateException.class.getSimpleName().equals(code)) {
			return RepositoryStatus.createCollisionError(taskRepository.getRepositoryUrl(), CloudDevTasksConnectorBundle.ID);
		} else if (AuthenticationException.class.getSimpleName().equals(code) || 
			  InsufficientPermissionsException.class.getSimpleName().equals(code)) {
			return RepositoryStatus.createLoginError(taskRepository.getRepositoryUrl(), CloudDevTasksConnectorBundle.ID);
		}
		return RepositoryStatus.createStatus(taskRepository, IStatus.ERROR, CloudDevTasksConnectorBundle.ID, error.getMessage());
	}

	public String getTaskUrl(String repositoryUrl, String taskId) {
		return getUri(CloudDevConstants.TASK, taskId);
	}

	
	public RepositoryConfiguration getRepositoryConfiguration(boolean forceRefresh, IProgressMonitor monitor) throws CoreException {
		if (repositoryConfiguration == null || forceRefresh) {
			this.repositoryConfiguration = getRepositoryConfiguration(monitor);
		}
		return repositoryConfiguration;
	}
	
	private RepositoryConfiguration getRepositoryConfiguration() {
		try {
			return getRepositoryConfiguration(false, new NullProgressMonitor());
		} catch (CoreException e) {
			return null;
		}
	}
	
	private RepositoryConfiguration getRepositoryConfiguration(IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		final String uri = getUri(CloudDevConstants.REPOSITORY_CONTEXT);
                
                getProfile(monitor);
                
                CloudDevResults result = getCloudDevResults(uri,  monitor);
		return result.getRepositoryConfiguration();
	}

	public Task getTaskById(String id, IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		if (id == null || id.length() == 0) {
            throw new IllegalStateException();
		}
		final String uri = getUri(CloudDevConstants.TASK, id);
		CloudDevResults result = getCloudDevResults(uri, monitor);
		return result.getTask();
	}
	
	private String getUri(final String uri) {
		StringBuilder builder = new StringBuilder();
		final String url = taskRepository.getUrl(); 
		builder.append(url);
		if (!url.endsWith(CloudDevConstants.SEPARATOR)) {
			builder.append(CloudDevConstants.SEPARATOR);
		}
		builder.append(uri);
		builder.append(CloudDevConstants.SEPARATOR);
		return builder.toString();
	}


	private String getUri(final String arg1, final String arg2) {
		StringBuilder builder = new StringBuilder();
		final String url = taskRepository.getUrl(); 
		builder.append(url);
		if (!url.endsWith(CloudDevConstants.SEPARATOR)) {
			builder.append(CloudDevConstants.SEPARATOR);
		}
		builder.append(arg1);
		builder.append(CloudDevConstants.SEPARATOR);
		builder.append(arg2);
		builder.append(CloudDevConstants.SEPARATOR);
		return builder.toString();
	}
	
	private String getUri(final String arg1, final String arg2, final String arg3) {
		StringBuilder builder = new StringBuilder();
		final String url = taskRepository.getUrl(); 
		builder.append(url);
		if (!url.endsWith(CloudDevConstants.SEPARATOR)) {
			builder.append(CloudDevConstants.SEPARATOR);
		}
		builder.append(arg1);
		builder.append(CloudDevConstants.SEPARATOR);
		builder.append(arg2);
		builder.append(CloudDevConstants.SEPARATOR);
		builder.append(arg3);
		builder.append(CloudDevConstants.SEPARATOR);
		return builder.toString();
	}

	public void validate(IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		final String uri = getUri(CloudDevConstants.REPOSITORY_CONTEXT);
		CloudDevResults results = getCloudDevResults(uri, monitor);
		if (results == null || results.getRepositoryConfiguration() == null) {
			throw new CoreException(RepositoryStatus.createStatus(taskRepository, IStatus.ERROR, CloudDevTasksConnectorBundle.ID, Resources.validationError));
		}
	}

	public InputStream retrieveAttachment(String id, TaskAttribute attachmentAttribute, IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		final String uri = getUri(CloudDevConstants.ATTACHMENT, attachmentAttribute.getValue(), CloudDevConstants.ATTACHMENT_FULL);
		CloudDevResults results = getCloudDevResults(uri, monitor);
		Attachment attachment = results.getAttachment();
		return new ByteArrayInputStream(attachment.getAttachmentData());
	}
	
	public void postAttachment(String id, AbstractTaskAttachmentSource source, String comment,
			TaskAttribute attachmentAttribute, IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		final String uri = getUri(CloudDevConstants.ATTACHMENT);
		Task task = new Task();
		task.setId(Integer.valueOf(id));
		SaveAttachmentArguments attachmentArguments = getAttachmentArguments(id, getVersion(attachmentAttribute), source, comment, attachmentAttribute);
		CloudDevResults results = getCloudDevResults(uri, serializeAsString(attachmentArguments), monitor);
		AttachmentHandle handle = results.getAttachmentHandle();
		assert handle != null;
	}
	
	private String getVersion(TaskAttribute attachmentAttribute) {
		TaskAttribute root = attachmentAttribute.getParentAttribute();
		TaskAttribute versionAttribute = root.getAttribute(CloudDevAttribute.VERSION.getTaskName());
		if (versionAttribute != null) {
			return versionAttribute.getValue();
		}
		return null;
	}

	private SaveAttachmentArguments getAttachmentArguments(final String id, final String version, AbstractTaskAttachmentSource source, 
			String comment, TaskAttribute attachmentAttribute) throws CoreException {

		TaskHandle handle = new TaskHandle(new Integer(id), version);
		String description = source.getDescription();
		String contentType = source.getContentType();
		String filename = source.getName();
		TaskAttachmentMapper mapper = TaskAttachmentMapper.createFrom(attachmentAttribute);
		if (mapper.getDescription() != null) {
			description = mapper.getDescription(); 
		} 
		if (mapper.getContentType() != null) {
			contentType = mapper.getContentType(); 
		}
		if (mapper.getFileName() != null) {
			filename = mapper.getFileName(); 
		}
		
		Attachment attachment = new Attachment();
		attachment.setDescription(description);
		attachment.setMimeType(contentType);
		attachment.setFilename(filename);
		attachment.setTaskHandle(handle);
		try {
			byte[] byteArray = toByteArray(source.createInputStream(null));
			attachment.setAttachmentData(byteArray);
			attachment.setByteSize(byteArray.length);
		} catch (IOException e) {
			throw new CoreException(new RepositoryStatus(taskRepository, IStatus.ERROR, CloudDevTasksConnectorBundle.ID, RepositoryStatus.ERROR_IO, e.getMessage(), e));
		}
		
		SaveAttachmentArguments attachmentArguments = new SaveAttachmentArguments(handle, attachment);
		Comment c = new Comment();
		c.setCommentText(comment);
		if (mapper.getComment() != null) {
			c.setCommentText(mapper.getComment());
		}
		attachmentArguments.setComment(c);
		return attachmentArguments;
	}
	
	private byte[] toByteArray(InputStream is) throws IOException {
        final ByteArrayOutputStream buf = new ByteArrayOutputStream();
        final byte[] bytes = new byte[1024];
        
		for (int count = is.read(bytes); count != -1; count = is.read(bytes)) {
			buf.write(bytes, 0, count);
		}

        return buf.toByteArray();
	}

	public RepositoryResponse newTask(TaskData taskData, IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		final String uri = getUri(CloudDevConstants.TASK);
		Task task = new Task();
		updateTask(task, taskData);
		CloudDevResults results = getCloudDevResults(uri, serializeAsString(task), monitor);
		Task newTask = results.getTask();
		return new RepositoryResponse(ResponseKind.TASK_CREATED, newTask.getId() + "");
	}

	public RepositoryResponse modifyTaskById(TaskData taskData, IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		final String id = taskData.getTaskId();
		final String uri = getUri(CloudDevConstants.TASK, id);
		Task task = new Task();
		task.setId(Integer.valueOf(id));
		updateTask(task, taskData);
		CloudDevResults results = getCloudDevResults(uri, serializeAsString(task), monitor);
		task = results.getTask();
		return new RepositoryResponse(ResponseKind.TASK_UPDATED, task.getId() + "");
	}

	private void updateTask(Task task, TaskData taskData) throws CoreException {
		final TaskAttribute root = taskData.getRoot();
		final TaskAttributeMapper mapper = taskData.getAttributeMapper();
		
		TaskAttribute taskAttribute = root.getAttribute(CloudDevAttribute.SHORT_DESCRIPTION.getTaskName());
		if (taskAttribute != null) {
			String value = taskAttribute.getValue();
			if (!equal(value, task.getShortDescription())) {
				task.setShortDescription(value);
			}
		}

		taskAttribute = root.getAttribute(CloudDevAttribute.DESCRIPTION.getTaskName());
		if (taskAttribute != null) {
			String value = taskAttribute.getValue();
			if (!equal(value, task.getDescription())) {
				task.setDescription(value);
			}
		}
		
		taskAttribute = root.getAttribute(CloudDevAttribute.STATUS_NEW.getTaskName());
		if (taskAttribute != null) {
			TaskStatus status = getTaskStatus(taskAttribute.getValue());
			if (!equal(status, task.getStatus())) {
				task.setStatus(status);
			}
		}
		
		taskAttribute = root.getAttribute(CloudDevAttribute.VERSION.getTaskName());
		if (taskAttribute != null) {
			task.setVersion(taskAttribute.getValue());
		}

		taskAttribute = root.getAttribute(CloudDevAttribute.TASK_TYPE.getTaskName());
		if (taskAttribute != null) {
			String value = taskAttribute.getValue();
			if (!equal(value, task.getTaskType())) {
				task.setTaskType(value);
			}
		}

		taskAttribute = root.getAttribute(CloudDevAttribute.PRIORITY.getTaskName());
		if (taskAttribute != null) {
			Priority priority = getPriority(taskAttribute.getValue());
			if (!equal(priority, task.getPriority())) {
				task.setPriority(priority);
			}
		}

		taskAttribute = root.getAttribute(CloudDevAttribute.SEVERITY.getTaskName());
		if (taskAttribute != null) {
			TaskSeverity taskSeverity = getTaskSeverity(taskAttribute.getValue());
			if (!equal(taskSeverity, task.getSeverity())) {
				task.setSeverity(taskSeverity);
			}
		}

		Product product = null;
		taskAttribute = root.getAttribute(CloudDevAttribute.PRODUCT.getTaskName());
		if (taskAttribute != null) {
			product = getProduct(taskAttribute.getValue());
			if (!equal(product, task.getProduct())) {
				task.setProduct(product);
			}
		}

		taskAttribute = root.getAttribute(CloudDevAttribute.COMPONENT.getTaskName());
		if (taskAttribute != null) {
			Component component = getComponent(product, taskAttribute.getValue());
			if (!equal(component, task.getComponent())) {
				task.setComponent(component);
			}
		}
		
		taskAttribute = root.getAttribute(CloudDevAttribute.MILESTONE.getTaskName());
		if (taskAttribute != null) {
			Milestone milestone = getMilestone(product, taskAttribute.getValue());
			if (!equal(milestone, task.getMilestone())) {
				task.setMilestone(milestone);
			}
		}

		taskAttribute = root.getAttribute(CloudDevAttribute.ITERATION.getTaskName());
		if (taskAttribute != null) {
			Iteration iteration = getIteration(taskAttribute.getValue());
			if (!equal(iteration, task.getIteration())) {
				task.setIteration(iteration);
			}
		}

		taskAttribute = root.getAttribute(CloudDevAttribute.ESTIMATED_TIME.getTaskName());
		if (taskAttribute != null) {
			BigDecimal estimatedTime = null;
			try {
				estimatedTime = new BigDecimal(taskAttribute.getValue());
			} catch (NumberFormatException e) {
				// do nothing
			}
			if (!equal(estimatedTime, task.getEstimatedTime())) {
				task.setEstimatedTime(estimatedTime);
			}
		}

		taskAttribute = root.getAttribute(CloudDevAttribute.REMAINING_TIME.getTaskName());
		if (taskAttribute != null) {
			BigDecimal remainingTime = null;
			try {
				remainingTime = new BigDecimal(taskAttribute.getValue());
			} catch (NumberFormatException e) {
				// do nothing
			}
			if (!equal(remainingTime, task.getRemainingTime())) {
				task.setRemainingTime(remainingTime);
			}
		}

		taskAttribute = root.getAttribute(CloudDevAttribute.NEW_COMMENT.getTaskName());
		if (taskAttribute != null) {
			String value = taskAttribute.getValue();
			if (value != null && value.length() > 0) {
				task.addComment(value);
			}
		}
		
		taskAttribute = root.getAttribute(CloudDevAttribute.ASSIGNEE.getTaskName());
		if (taskAttribute != null) {
			String value = taskAttribute.getValue();
			TaskUserProfile assignee = getUser(value);
			if (!equal(assignee, task.getAssignee())) {
				task.setAssignee(assignee);
			}
		}
		
		taskAttribute = root.getAttribute(CloudDevAttribute.CC.getTaskName());
		if (taskAttribute != null) {
			List<String> values = taskAttribute.getValues();
			List<TaskUserProfile> watchers = new ArrayList<TaskUserProfile>(values.size());
			for (String value : values) {
				TaskUserProfile watcher = getUser(value);
				if (watcher != null) {
					watchers.add(watcher);
				}
			}
			task.setWatchers(watchers);
		}
		
		taskAttribute = root.getAttribute(CloudDevAttribute.SUBTASKS.getTaskName());
		if (taskAttribute != null) {
			String value = taskAttribute.getValue();
			if (!equal(value, getTaskIds(task.getSubTasks()))) {
				List<Task> subtasks = getTaskList(value);
				task.setSubTasks(subtasks);
			}
		}

		taskAttribute = root.getAttribute(CloudDevAttribute.PARENT_TASK.getTaskName());
		if (taskAttribute != null) {
			String value = taskAttribute.getValue();
			if (!equal(value, getTaskId(task.getParentTask()))) {
				Task parentTask = getTaskById(value, null);
				task.setParentTask(parentTask);
			}
		}

		taskAttribute = root.getAttribute(CloudDevAttribute.KEYWORDS.getTaskName());
		if (taskAttribute != null) {
			List<String> values = taskAttribute.getValues();
			List<Keyword> keywords = new ArrayList<Keyword>();
			for(String str : values) {
				keywords.add(getKeyword(str));
			}
			task.setKeywords(keywords);
		}

		taskAttribute = root.getAttribute(CloudDevAttribute.DEADLINE.getTaskName());
		if (taskAttribute != null) {
			Date deadline = mapper.getDateValue(taskAttribute);
			if (!equal(deadline, task.getDeadline())) {
				task.setDeadline(deadline);
			}
		}
		
		taskAttribute = root.getAttribute(CloudDevAttribute.FOUND_IN_RELEASE.getTaskName());
		if (taskAttribute != null) {
			String value = taskAttribute.getValue();
			value = value.length() == 0 ? null : value;
			if (!equal(value, task.getFoundInRelease())) {
				task.setFoundInRelease(value);
			}
		}
		
		Map<String, String> customFields = new HashMap<String, String>();
		for (FieldDescriptor customDescriptor : repositoryConfiguration.getCustomFields()) {
			final String key = customDescriptor.getName();
			taskAttribute = root.getAttribute(key); 
			if (taskAttribute != null) {
				String value = taskAttribute.getValue();
				value = value.length() == 0 ? null : value;
				customFields.put(key, value);
			}
		}
		task.setCustomFields(customFields);

		taskAttribute = root.getMappedAttribute(TaskAttribute.STATUS);
		if (taskAttribute != null) {
			task.setStatus(getStatus(taskAttribute.getValue()));
		}

		taskAttribute = root.getMappedAttribute(TaskAttribute.RESOLUTION);
		if (taskAttribute != null) {
			task.setResolution(getResolution(taskAttribute.getValue()));
		}

		TaskAttribute attributeOperation = root.getMappedAttribute(TaskAttribute.OPERATION);
		if (attributeOperation != null) {
			String value = attributeOperation.getValue();
			if (CloudDevOperation.DUPLICATE.getValue().equals(value)) {
				value = CloudDevOperation.RESOLVED.getValue();
				task.setResolution(getResolution(CloudDevOperation.DUPLICATE.getValue()));
			}
			TaskStatus status = getTaskStatus(value);
			if (status != null && !equal(status, task.getStatus())) {
				task.setStatus(status);
				// Clear resolution when reopened
				if (CloudDevOperation.REOPENED.getValue().equals(status.getValue())) {
					task.setResolution(null);
					task.setDuplicateOf(null);
				}
			}
			
			TaskAttribute originalOperation = root.getAttribute(TaskAttribute.PREFIX_OPERATION + attributeOperation.getValue());
			if (originalOperation != null) {
				String inputAttributeId = originalOperation.getMetaData().getValue(TaskAttribute.META_ASSOCIATED_ATTRIBUTE_ID);
				if (inputAttributeId != null && !inputAttributeId.equals("")) { //$NON-NLS-1$  
					TaskAttribute inputAttribute = attributeOperation.getTaskData().getRoot().getAttribute(inputAttributeId);
					if (inputAttribute != null) {
						if (inputAttribute.getOptions().size() > 0) {
							String sel = inputAttribute.getValue();   
							TaskResolution resolution = getResolution(inputAttribute.getOption(sel));
							if (resolution != null && !equal(resolution, task.getResolution())) {
								task.setResolution(resolution);
							}
						} else {
							String duplicateOf = inputAttribute.getValue();
							Task duplicateTask = task.getDuplicateOf();
							if (!equal(duplicateOf, duplicateTask == null ? "" : duplicateTask.getId() + "")) {
								Task newDuplicateTask = getTaskById(duplicateOf, new NullProgressMonitor());
								task.setDuplicateOf(newDuplicateTask);
							}
						}
					}
				}
			}
		}
	}
	
	private Keyword getKeyword(final String value) {
		if (value != null) {
			for (Keyword keyword : getRepositoryConfiguration().getKeywords()) {
				if (value.equals(keyword.getName())) {
					return keyword;
				}
			}
		}
		return null;
	}

	private List<Task> getTaskList(final String value) throws CoreException {
		if (value != null) {
			List<Task> list = new ArrayList<Task>();
			StringTokenizer t = new StringTokenizer(value, ", ");
			while (t.hasMoreTokens()) {
				String id = t.nextToken();
				Task task = getTaskById(id, new NullProgressMonitor());
				list.add(task);
			}
			return list;
		}
		return null;
	}

	private TaskStatus getTaskStatus(final String value) {
		if (value != null) {
			for (TaskStatus status : getRepositoryConfiguration().getStatuses()) {
				if (value.equals(status.getValue())) {
					return status;
				}
			}
		}
		return null;
	}

	private TaskResolution getResolution(final String value) {
		if (value != null) {
			for (TaskResolution resolution : getRepositoryConfiguration().getResolutions()) {
				if (value.equals(resolution.getValue())) {
					return resolution;
				}
			}
		}
		return null;
	}

	private Priority getPriority(final String value) {
		if (value != null) {
			for (Priority priority : getRepositoryConfiguration().getPriorities()) {
				if (value.equals(priority.getValue())) {
					return priority;
				}
			}
		}
		return null;
	}

	private TaskSeverity getTaskSeverity(final String value) {
		if (value != null) {
			for (TaskSeverity severity : getRepositoryConfiguration().getSeverities()) {
				if (value.equals(severity.getValue())) {
					return severity;
				}
			}
		}
		return null;
	}

	private Product getProduct(final String value) {
		if (value != null) {
			for (Product product : getRepositoryConfiguration().getProducts()) {
				if (value.equals(product.getName())) {
					return product;
				}
			}
		}
		return null;
	}

	private TaskStatus getStatus(final String value) {
		if (value != null) {
			for (TaskStatus status : getRepositoryConfiguration().getStatuses()) {
				if (value.equals(status.getValue())) {
					return status;
				}
			}
		}
		return null;
	}

	private Component getComponent(final Product product, final String value) {
		if (value != null) {
			List<Component> components = product != null ? product.getComponents() : getRepositoryConfiguration().getComponents();
			for (Component component : components) {
				if (value.equals(component.getName())) {
					return component;
				}
			}
		}
		return null;
	}

	private Milestone getMilestone(final Product product, final String value) {
		if (value != null) {
			List<Milestone> milestones = product != null ? product.getMilestones() : getRepositoryConfiguration().getMilestones();
			for (Milestone milestone : milestones) {
				if (value.equals(milestone.getValue())) {
					return milestone;
				}
			}
		}
		return null;
	}

	private Iteration getIteration(final String value) {
		if (value != null) {
			for (Iteration iteration : getRepositoryConfiguration().getIterations()) {
				if (value.equals(iteration.getValue())) {
					return iteration;
				}
			}
		}
		return null;
	}
	
	private TaskUserProfile getUser(final String value) {
		if (value != null) {
			for (TaskUserProfile profile : getRepositoryConfiguration().getUsers()) {
				if (value.equals(profile.getLoginName()) || value.equals(profile.getRealname())) {
					return profile;
				}
			}
		}
		return null;
	}
	
	private String serializeAsString(Object obj) throws CoreException {
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.writeValueAsString(obj);
		} catch (JsonGenerationException e) {
			throw new CoreException(new RepositoryStatus(taskRepository, IStatus.ERROR, CloudDevTasksConnectorBundle.ID, RepositoryStatus.ERROR_IO, e.getMessage(), e));
		} catch (JsonMappingException e) {
			throw new CoreException(new RepositoryStatus(taskRepository, IStatus.ERROR, CloudDevTasksConnectorBundle.ID, RepositoryStatus.ERROR_IO, e.getMessage(), e));
		} catch (IOException e) {
			throw new CoreException(new RepositoryStatus(taskRepository, IStatus.ERROR, CloudDevTasksConnectorBundle.ID, RepositoryStatus.ERROR_IO, e.getMessage(), e));
		}
	}
	
	public void createDefaultTaskAttributes(RepositoryConfiguration repositoryConfiguration, TaskData taskData) {
		boolean isExisting = !taskData.isNew();
		
		TaskAttribute rootAttribute = taskData.getRoot();
		TaskAttributeMetaData rootMetaData = rootAttribute.getMetaData();
		rootMetaData.setType(TaskAttribute.TYPE_SHORT_TEXT);
		rootMetaData.setKind(TaskAttribute.KIND_DEFAULT);
		
		createAttribute(taskData, CloudDevAttribute.SHORT_DESCRIPTION);

		createAttribute(taskData, CloudDevAttribute.DESCRIPTION);

		createAttribute(taskData, CloudDevAttribute.CREATION_DATE);
		
		createAttribute(taskData, CloudDevAttribute.MODIFIED_DATE);

		if (isExisting) {
			createAttribute(taskData, CloudDevAttribute.VERSION);
		}

		TaskAttribute attribute;
		attribute = createAttribute(taskData, CloudDevAttribute.TASK_TYPE);
		for (String type : repositoryConfiguration.getTaskTypes()) {
			attribute.putOption(type, type);
		}
		attribute.setValue(repositoryConfiguration.getDefaultType());

		if (isExisting) {
			attribute = createAttribute(taskData, CloudDevAttribute.STATUS);
		} else {
			attribute = createAttribute(taskData, CloudDevAttribute.STATUS_NEW);
			for (TaskStatus status : repositoryConfiguration.computeValidStatuses(null)) {
				attribute.putOption(status.getValue(), status.getValue());
			}
			if (repositoryConfiguration.getDefaultStatus() != null) {
				attribute.setValue(repositoryConfiguration.getDefaultStatus().getValue());
			}
		}
		
		if (isExisting) {
			attribute = createAttribute(taskData, CloudDevAttribute.RESOLUTION);
			for (TaskResolution resolution : repositoryConfiguration.getResolutions()) {
				attribute.putOption(resolution.getValue(), resolution.getValue());
			}
		}
		
		attribute = createAttribute(taskData, CloudDevAttribute.PRIORITY);
		for (Priority priority : repositoryConfiguration.getPriorities()) {
			attribute.putOption(priority.getValue(), priority.getValue());
		}
		if (repositoryConfiguration.getDefaultPriority() != null) {
			attribute.setValue(repositoryConfiguration.getDefaultPriority().getValue());
		}
		
		attribute = createAttribute(taskData, CloudDevAttribute.SEVERITY);
		for (TaskSeverity severity : repositoryConfiguration.getSeverities()) {
			attribute.putOption(severity.getValue(), severity.getValue());
		}
		if (repositoryConfiguration.getDefaultSeverity() != null) {
			attribute.setValue(repositoryConfiguration.getDefaultSeverity().getValue());
		}
		

		attribute = createAttribute(taskData, CloudDevAttribute.PRODUCT);
		for (Product product : repositoryConfiguration.getProducts()) {
			attribute.putOption(product.getName(), product.getName());
		}
		final Product defaultProduct = repositoryConfiguration.getDefaultProduct(); 
		if (defaultProduct != null) {
			attribute.setValue(defaultProduct.getName());
		}

		attribute = createAttribute(taskData, CloudDevAttribute.COMPONENT);
		if (!isExisting && defaultProduct != null) {
			for (Component component : defaultProduct.getComponents()) {
				attribute.putOption(component.getName(), component.getName());
			}
			if (defaultProduct.getDefaultComponent() != null) {
				attribute.setValue(defaultProduct.getDefaultComponent().getName());
			}
		}

		attribute = createAttribute(taskData, CloudDevAttribute.MILESTONE);
		if (!isExisting && defaultProduct != null) {
			for (Milestone milestone : defaultProduct.getMilestones()) {
				attribute.putOption(milestone.getValue(), milestone.getValue());
			}
			if (defaultProduct.getDefaultMilestone() != null) {
				attribute.setValue(defaultProduct.getDefaultMilestone().getValue());
			}
		}

		createAttribute(taskData, CloudDevAttribute.FOUND_IN_RELEASE);

		attribute = createAttribute(taskData, CloudDevAttribute.ITERATION);
		for (Iteration iteration : repositoryConfiguration.getActiveIterations()) {
			attribute.putOption(iteration.getValue(), iteration.getValue());
		}
		if (repositoryConfiguration.getDefaultIteration() != null) {
			attribute.setValue(repositoryConfiguration.getDefaultIteration().getValue());
		}

		createAttribute(taskData, CloudDevAttribute.DEADLINE);

		createAttribute(taskData, CloudDevAttribute.ESTIMATED_TIME);

		if (isExisting) {
			createAttribute(taskData, CloudDevAttribute.REMAINING_TIME);
		}

		attribute = createAttribute(taskData, CloudDevAttribute.KEYWORDS);
		for (Keyword keyword : repositoryConfiguration.getKeywords()) {
			attribute.putOption(keyword.getName(), keyword.getName());
		}

		createAttribute(taskData, CloudDevAttribute.ASSIGNEE);			

		attribute = createAttribute(taskData, CloudDevAttribute.CC);
		for (TaskUserProfile user : repositoryConfiguration.getUsers()) {
			taskRepository.createPerson(user.getLoginName());
			attribute.putOption(user.getLoginName(), user.getLoginName());
		}
		
		if (isExisting) {
			createAttribute(taskData, CloudDevAttribute.URL);

			createAttribute(taskData, CloudDevAttribute.SUBTASKS);

			createAttribute(taskData, CloudDevAttribute.PARENT_TASK);

			createAttribute(taskData, CloudDevAttribute.SUM_OF_SUBTASKS_ESTIMATED_TIME);

			createAttribute(taskData, CloudDevAttribute.SUM_OF_SUBTASKS_TIME_SPENT);

			createAttribute(taskData, CloudDevAttribute.NEW_COMMENT);
			
			createAttribute(taskData, CloudDevAttribute.DUPLICATE_OF);

			createAttribute(taskData, CloudDevAttribute.DUPLICATES);

			// implicit
			createAttribute(taskData, CloudDevAttribute.REPORTER);			
		}
		
		for (FieldDescriptor customDescriptor : repositoryConfiguration.getCustomFields()) {
			if (isExisting || (!isExisting && customDescriptor.isAvailableForNewTasks())) {
				createAttribute(taskData, customDescriptor);
			}
		}
	}
	
	public void populateTaskAttributes(TaskData taskData, Task task) {
		TaskAttributeMapper mapper = taskData.getAttributeMapper();
		TaskAttribute rootAttribute = taskData.getRoot();
		TaskAttributeMetaData rootMetaData = rootAttribute.getMetaData();
		rootMetaData.setLabel(task.getShortDescription());

		TaskAttribute attribute;
		attribute = rootAttribute.getAttribute(CloudDevAttribute.SHORT_DESCRIPTION.getTaskName());
		attribute.setValue(task.getShortDescription());
		
		attribute = rootAttribute.getAttribute(CloudDevAttribute.DESCRIPTION.getTaskName());
		attribute.setValue(task.getDescription());
		
		attribute = rootAttribute.getAttribute(CloudDevAttribute.CREATION_DATE.getTaskName());
		mapper.setDateValue(attribute, task.getCreationDate());
		
		attribute = rootAttribute.getAttribute(CloudDevAttribute.MODIFIED_DATE.getTaskName());
		mapper.setDateValue(attribute, task.getModificationDate());

		attribute = rootAttribute.getAttribute(CloudDevAttribute.VERSION.getTaskName());
		if (attribute != null) {
			attribute.setValue(task.getVersion());
		}		

		attribute = rootAttribute.getAttribute(CloudDevAttribute.TASK_TYPE.getTaskName());
		attribute.setValue(task.getTaskType());

		if (task.getStatus() != null) {
			attribute = rootAttribute.getAttribute(CloudDevAttribute.STATUS.getTaskName());
			attribute.setValue(task.getStatus().getValue());
		}

		if (task.getResolution() != null) {
			attribute = rootAttribute.getAttribute(CloudDevAttribute.RESOLUTION.getTaskName());
			attribute.setValue(task.getResolution().getValue());
		}

		if (task.getPriority() != null) {
			attribute = rootAttribute.getAttribute(CloudDevAttribute.PRIORITY.getTaskName());
			attribute.setValue(task.getPriority().getValue());
		}

		if (task.getSeverity() != null) {
			attribute = rootAttribute.getAttribute(CloudDevAttribute.SEVERITY.getTaskName());
			attribute.setValue(task.getSeverity().getValue());
		}

		if (task.getProduct() != null) {
			attribute = rootAttribute.getAttribute(CloudDevAttribute.PRODUCT.getTaskName());
			attribute.setValue(task.getProduct().getName());
		}
		
		attribute = rootAttribute.getAttribute(CloudDevAttribute.DEADLINE.getTaskName());
		mapper.setDateValue(attribute, task.getDeadline());

		attribute = rootAttribute.getAttribute(CloudDevAttribute.FOUND_IN_RELEASE.getTaskName());
		setStringValue(attribute, task.getFoundInRelease());

		if (task.getIteration() != null) {
			attribute = rootAttribute.getAttribute(CloudDevAttribute.ITERATION.getTaskName());
			attribute.setValue(task.getIteration().getValue());
		}
		
		attribute = rootAttribute.getAttribute(CloudDevAttribute.URL.getTaskName());
		attribute.setValue(task.getUrl());

		attribute = rootAttribute.getAttribute(CloudDevAttribute.ESTIMATED_TIME.getTaskName());
		// TODO parse BigDecimal in hours to readable (8 == 1d, 4 == 4h, 4.25 == 4h 15m)
		BigDecimal estimatedTime = task.getEstimatedTime();
		if (estimatedTime == null) {
			attribute.clearValues();
		} else {
			attribute.setValue(estimatedTime.toString());
		}

		attribute = rootAttribute.getAttribute(CloudDevAttribute.REMAINING_TIME.getTaskName());
		// TODO parse BigDecimal in hours to readable (8 == 1d, 4 == 4h, 4.25 == 4h 15m)
		BigDecimal remainingTime = task.getRemainingTime();
		if (remainingTime == null) {
			attribute.clearValues();
		} else {
			attribute.setValue(remainingTime.toString());
		}

		attribute = rootAttribute.getAttribute(CloudDevAttribute.SUM_OF_SUBTASKS_ESTIMATED_TIME.getTaskName());
		// TODO parse BigDecimal in hours to readable (8 == 1d, 4 == 4h, 4.25 == 4h 15m)
		BigDecimal sumOfSubtasksEstimatedTime = task.getSumOfSubtasksEstimatedTime();
		if (sumOfSubtasksEstimatedTime == null) {
			attribute.clearValues();
		} else {
			attribute.setValue(sumOfSubtasksEstimatedTime.toString());
		}

		attribute = rootAttribute.getAttribute(CloudDevAttribute.SUM_OF_SUBTASKS_TIME_SPENT.getTaskName());
		// TODO parse BigDecimal in hours to readable (8 == 1d, 4 == 4h, 4.25 == 4h 15m)
		BigDecimal sumOfSubtasksTimeSpent = task.getSumOfSubtasksTimeSpent();
		if (sumOfSubtasksTimeSpent == null) {
			attribute.clearValues();
		} else {
			attribute.setValue(sumOfSubtasksTimeSpent.toString());
		}

		final String subtasks = getTaskIds(task.getSubTasks());
		if (subtasks != null) {
			attribute = rootAttribute.getAttribute(CloudDevAttribute.SUBTASKS.getTaskName());
			attribute.setValue(subtasks);
		}
		
		if (task.getParentTask() != null) {
			attribute = rootAttribute.getAttribute(CloudDevAttribute.PARENT_TASK.getTaskName());
			attribute.setValue(task.getParentTask().getId() + "");
		}
		
		if (task.getDuplicateOf() != null) {
			attribute = rootAttribute.getAttribute(CloudDevAttribute.DUPLICATE_OF.getTaskName());
			attribute.setValue(task.getDuplicateOf().getId() + "");
		}
		
		List<Task> deuplicates = task.getDuplicates();
		if (deuplicates != null && deuplicates.size() > 0) {
			attribute = rootAttribute.getAttribute(CloudDevAttribute.DUPLICATES.getTaskName());
			attribute.setValue(getTaskIds(deuplicates));
		}
		
		List<Keyword> keywords = task.getKeywords();
		if (keywords != null && keywords.size() > 0) {
			attribute = rootAttribute.getAttribute(CloudDevAttribute.KEYWORDS.getTaskName());
			attribute.setValues(getKeywordList(keywords));
		}

		if (task.getAssignee() != null) {
			attribute = rootAttribute.getAttribute(CloudDevAttribute.ASSIGNEE.getTaskName());
			IRepositoryPerson person = taskRepository.createPerson(task.getAssignee().getLoginName());
			person.setName(task.getAssignee().getRealname());
			mapper.setRepositoryPerson(attribute, person);
		}

		if (task.getReporter() != null) {
			attribute = rootAttribute.getAttribute(CloudDevAttribute.REPORTER.getTaskName());
			IRepositoryPerson person = taskRepository.createPerson(task.getReporter().getLoginName());
			person.setName(task.getReporter().getRealname());
			mapper.setRepositoryPerson(attribute, person);
		}
		
		List<TaskUserProfile> watchers = task.getWatchers();
		if (watchers != null && watchers.size() > 0) {
			attribute = rootAttribute.getAttribute(CloudDevAttribute.CC.getTaskName());
			List<String> cc = new ArrayList<String>();
			for (TaskUserProfile watcher : watchers) {
				cc.add(watcher.getLoginName());
			}
			attribute.setValues(cc);
		}

		// add comments
		int count = 1;
		for (Comment comment : task.getComments()) {
			TaskCommentMapper commentMapper = new TaskCommentMapper();
			commentMapper.setCommentId(comment.getId() + "");
			TaskUserProfile author = comment.getAuthor();
			if (author != null) {
				IRepositoryPerson person = taskRepository.createPerson(author.getLoginName());
				person.setName(author.getRealname());
				commentMapper.setAuthor(person);
			}
			commentMapper.setText(comment.getCommentText());
			commentMapper.setCreationDate(comment.getCreationDate());
			commentMapper.setNumber(count);
			
			TaskAttribute taskAttribute = taskData.getRoot().createAttribute(TaskAttribute.PREFIX_COMMENT + count);
			commentMapper.applyTo(taskAttribute);
			count++;
		}
		
		// add attachment
		count = 1;
		List<Attachment> attachments = task.getAttachments();
		if (attachments != null && attachments.size() > 0) {
			for (Attachment attachment : attachments) {
				TaskAttachmentMapper attachmentMapper = new TaskAttachmentMapper();
				attachmentMapper.setAttachmentId(attachment.getId() + "");
				TaskUserProfile submitter = attachment.getSubmitter();
				if (submitter != null) {
					IRepositoryPerson person = taskRepository.createPerson(submitter.getLoginName());
					person.setName(submitter.getRealname());
					attachmentMapper.setAuthor(person);
				}
				attachmentMapper.setDescription(attachment.getDescription());
				attachmentMapper.setCreationDate(attachment.getCreationDate());
				attachmentMapper.setContentType(attachment.getMimeType());
				attachmentMapper.setFileName(attachment.getFilename());
				attachmentMapper.setLength((Long.parseLong(String.valueOf(attachment.getByteSize()))));
				attachmentMapper.setUrl(attachment.getUrl());

				TaskAttribute taskAttribute = taskData.getRoot().createAttribute(TaskAttribute.PREFIX_ATTACHMENT + count);
				attachmentMapper.applyTo(taskAttribute);
				count++;
			}
		}
		
		for (Map.Entry<String, String> entry : task.getCustomFields().entrySet()) {
			attribute = rootAttribute.getAttribute(entry.getKey());
			if (attribute != null) {
				setStringValue(attribute, entry.getValue());
			}
		}
		
		updateAttributeOptions(taskData, task);
		
		addValidOperations(taskData);
	}

	private void updateAttributeOptions(TaskData taskData, Task task) {
		final TaskAttribute root = taskData.getRoot();
		TaskAttribute taskAttribute = root.getAttribute(CloudDevAttribute.PRODUCT.getTaskName());
		if (taskAttribute != null) {
			Product product = getProduct(taskAttribute.getValue());
			if (product != null) {
				TaskAttribute componentAttribute = root.getAttribute(CloudDevAttribute.COMPONENT.getTaskName());
				componentAttribute.clearOptions();
				for (Component comp : product.getComponents()) {
					componentAttribute.putOption(comp.getName(), comp.getName());
				}
				if (task.getComponent() != null) {
					componentAttribute.setValue(task.getComponent().getName());
				} else if (product.getDefaultComponent() != null) {
					componentAttribute.setValue(product.getDefaultComponent().getName());
				}
				
				TaskAttribute milestoneAttribute = root.getAttribute(CloudDevAttribute.MILESTONE.getTaskName());
				milestoneAttribute.clearOptions();
				for (Milestone milestone : product.getMilestones()) {
					milestoneAttribute.putOption(milestone.getValue(), milestone.getValue());
				}
				if (task.getMilestone() != null) {
					milestoneAttribute.setValue(task.getMilestone().getValue());
				} else if (product.getDefaultMilestone() != null) {
					milestoneAttribute.setValue(product.getDefaultMilestone().getValue());
				}
			}
		}
	}

	private void addValidOperations(TaskData taskData) {
		final TaskAttribute root = taskData.getRoot();
		TaskAttribute statusAttribute = root.getAttribute(CloudDevAttribute.STATUS.getTaskName());
		RepositoryConfiguration config = getRepositoryConfiguration();
		TaskStatus status = config.getDefaultStatus();
		if (statusAttribute != null) {
			status = getStatus(statusAttribute.getValue());
		}
		//		UNCONFIRMED	    NEW ASSIGNED RESOLVED# 
		//		NEW				ASSIGNED RESOLVED#
		//		ASSIGNED		NEW RESOLVED#
		//		REOPENED		NEW ASSIGNED RESOLVED# 
		//		RESOLVED		REOPENED VERIFIED# CLOSED#
		//		VERIFIED		REOPENED RESOLVED# CLOSED#
		//		CLOSED			REOPENED RESOLVED#
		
		List<CloudDevOperation> operations = new ArrayList<CloudDevOperation>();
		operations.add(CloudDevOperation.NONE);
		for (TaskStatus validStatus : config.computeValidStatuses(status)) {
			CloudDevOperation operation = CloudDevOperation.getByTaskStatus(validStatus);
			if (operation == CloudDevOperation.RESOLVED) {
				operations.add(operation);
				operations.add(CloudDevOperation.DUPLICATE);
			} else if (!status.getValue().equals(validStatus.getValue())) {
				operations.add(operation);
			}
		}
		
		Collections.sort(operations, new Comparator<CloudDevOperation>() {
			@Override
			public int compare(CloudDevOperation o1, CloudDevOperation o2) {
				return OPERATION_PREDEFINED_ORDER.indexOf(o1) - OPERATION_PREDEFINED_ORDER.indexOf(o2); 
			}
		});
		for (CloudDevOperation operation : operations) {
			addOperation(taskData, operation);
		}
	}

	private void addOperation(TaskData taskData, CloudDevOperation operation) {
		if (operation == null) {
			return;
		}
		
		final TaskAttribute root = taskData.getRoot();
		TaskAttribute operationAttribute = root.getAttribute(TaskAttribute.OPERATION);
		if (operationAttribute == null) {
			operationAttribute = root.createAttribute(TaskAttribute.OPERATION);
		}
		
		TaskAttribute attribute = root.createAttribute(TaskAttribute.PREFIX_OPERATION + operation.getValue());

		switch (operation) {
		case NONE:
			TaskAttribute statusAttribute = root.getMappedAttribute(TaskAttribute.STATUS);
			TaskAttribute resolutionAttribute = root.getMappedAttribute(TaskAttribute.RESOLUTION);
			String label = operation.getLabel();
			if (statusAttribute != null && resolutionAttribute != null) {
				label += " " + statusAttribute.getValue() + " " + resolutionAttribute.getValue();
			}

			TaskOperation.applyTo(attribute, operation.getValue(), label);
			// set as default
			TaskOperation.applyTo(operationAttribute, operation.getValue(), label);
			break;
		case RESOLVED:
			TaskOperation.applyTo(attribute, operation.getValue(), operation.getLabel());
			TaskAttribute resolvedInputAttribute = attribute.getTaskData().getRoot().createAttribute(operation.getInputId());
			resolvedInputAttribute.getMetaData().setType(operation.getInputType());
			attribute.getMetaData().putValue(TaskAttribute.META_ASSOCIATED_ATTRIBUTE_ID, operation.getInputId());
			for (TaskResolution resolution : getRepositoryConfiguration().getResolutions()) {
				// duplicate gets its own operation
				if (resolution.getValue().length() > 0 && resolution.getValue().compareTo("DUPLICATE") != 0) {
					resolvedInputAttribute.putOption(resolution.getValue(), resolution.getValue());
				}
			}
			resolvedInputAttribute.setValue(getRepositoryConfiguration().getDefaultResolution().getValue());
			break;
		case DUPLICATE:
			TaskOperation.applyTo(attribute, operation.getValue(), operation.getLabel());
			if (operation.getInputId() != null) {
				TaskAttribute attrInput = root.getAttribute(operation.getInputId());
				if (attrInput == null) {
					attrInput = root.createAttribute(operation.getInputId());
				}
				attrInput.getMetaData().defaults().setReadOnly(false).setType(operation.getInputType());
				attribute.getMetaData().putValue(TaskAttribute.META_ASSOCIATED_ATTRIBUTE_ID, operation.getInputId());
			}
			break;
		default:
			TaskOperation.applyTo(attribute, operation.getValue(), operation.getLabel());
			break;
		}
	}
	
	private void setStringValue(TaskAttribute attribute, String value) {
		if (value == null) {
			attribute.clearValues();
		} else {
			attribute.setValue(value);
		}
	}

	private List<String> getKeywordList(List<Keyword> keywords) {
		List<String> list = new ArrayList<String>();
		for (Keyword keyword : keywords) {
			list.add(keyword.getName());
		}
		return list;
	}
	
	private String getTaskId(Task task) {
		if (task != null) {
			return task.getId() + "";
		}
		return "";
	}
	
	private String getTaskIds(List<Task> tasks) {
		if (tasks != null && tasks.size() > 0) {
			StringBuilder builder = new StringBuilder();
			for (Task task : tasks) {
				if (builder.length() > 0) {
					builder.append(",");
				}
				builder.append(task.getId());
			}
			return builder.toString();
		} else {
			return null;
		}
	}
	
	private TaskAttribute createAttribute(TaskData taskData, CloudDevAttribute attribute) {
		TaskAttribute taskAttribute = taskData.getRoot().createAttribute(attribute.getTaskName());
		TaskAttributeMetaData metaData = taskAttribute.getMetaData();
		metaData.setLabel(attribute.getLabel());
		metaData.setType(attribute.getType());
		metaData.setReadOnly(attribute.isReadonly());
		metaData.setKind(attribute.getKind());
		return taskAttribute;
	}

	private TaskAttribute createAttribute(TaskData taskData, FieldDescriptor descriptor) {
		TaskAttribute taskAttribute = taskData.getRoot().createAttribute(descriptor.getName());
		TaskAttributeMetaData metaData = taskAttribute.getMetaData();
		metaData.setLabel(descriptor.getName());
		// TODO custom attribute as attribute?
		metaData.setKind(TaskAttribute.KIND_DEFAULT);
		switch (descriptor.getFieldType()) {
		case TEXT:
			metaData.setType(TaskAttribute.TYPE_SHORT_TEXT);
			break;
		case SINGLE_SELECT:
			metaData.setType(TaskAttribute.TYPE_SINGLE_SELECT);
			for (String value : descriptor.getValueStrings()) {
				taskAttribute.putOption(value, value);
			}
			break;
		case MULTI_SELECT:
			metaData.setType(TaskAttribute.TYPE_MULTI_SELECT);
			for (String value : descriptor.getValueStrings()) {
				taskAttribute.putOption(value, value);
			}
			break;
		case LONG_TEXT:
			metaData.setType(TaskAttribute.TYPE_LONG_TEXT);
			break;
		case TIMESTAMP:
			metaData.setType(TaskAttribute.TYPE_DATETIME);
			break;
		case TASK_REFERENCE:
			metaData.setType(TaskAttribute.TYPE_TASK_DEPENDENCY);
			break;
			// TODO checkbox?
//		case CHECKBOX:
//			metaData.setType(TaskAttribute.TYPE_);
//			break;
		default:
			break;
		}
		return taskAttribute;
	}
	
    private static final boolean equal( final Object obj1, final Object obj2 )
    {
        boolean objectsAreEqual = false;
        
        if( obj1 == obj2 )
        {
            objectsAreEqual = true;
        }
        else if( obj1 != null && obj2 != null )
        {
            if( obj1.getClass().isArray() && obj2.getClass().isArray() )
            {
                objectsAreEqual = Arrays.equals( (Object[]) obj1, (Object[]) obj2 );
            }
            else
            {
                objectsAreEqual = obj1.equals( obj2 );
            }
        }

        return objectsAreEqual;
    }

	private static final class Resources extends NLS {
		public static String validationError;

		static {
			initializeMessages(CloudDevClient.class.getName(), Resources.class);
		}
	}


}
