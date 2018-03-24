package ut.com.isroot.stash.plugin;

import com.atlassian.bitbucket.event.branch.BranchCreationRequestedEvent;
import com.atlassian.bitbucket.hook.repository.RepositoryHookDetails;
import com.atlassian.bitbucket.hook.repository.RepositoryHookService;
import com.atlassian.bitbucket.i18n.I18nService;
import com.atlassian.bitbucket.i18n.KeyedMessage;
import com.atlassian.bitbucket.permission.Permission;
import com.atlassian.bitbucket.repository.Branch;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.setting.Settings;
import com.atlassian.bitbucket.setting.SettingsBuilder;
import com.atlassian.bitbucket.user.EscalatedSecurityContext;
import com.atlassian.bitbucket.user.SecurityService;
import com.atlassian.bitbucket.util.UncheckedOperation;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.isroot.stash.plugin.YaccBranchCreationListener;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ut.com.isroot.stash.plugin.mock.StubRepositoryHook;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Hiroyuki Wada
 * 
 */
public class YaccBranchCreationListenerTest {
    @Mock private RepositoryHookService repositoryHookService;

    @Mock private RepositoryHookDetails repositoryHookDetails;
    @Mock private Repository repository;
    @Mock private Branch branch;
    @Mock private SecurityService securityService;
    @Mock private EscalatedSecurityContext escalatedSecurityContextForRepositoryHook;
    @Mock private EscalatedSecurityContext escalatedSecurityContextForSettings;
    @Mock private PluginSettingsFactory pluginSettingsFactory;
    @Mock private PluginSettings pluginSettings;
    @Mock private SettingsBuilder settingsBuilder;
    @Mock private Settings settings;
    @Mock private I18nService i18nService;
    @Mock private BranchCreationRequestedEvent event;
    @Mock private KeyedMessage message;
    private Map<String, Object> settingsMap = new HashMap<String, Object>();

    private StubRepositoryHook repositoryHook;

    private YaccBranchCreationListener yaccBranchCreationListener;


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        yaccBranchCreationListener = new YaccBranchCreationListener(
                pluginSettingsFactory, securityService, repositoryHookService, i18nService);

        repositoryHook = new StubRepositoryHook();
        repositoryHook.setDetails(repositoryHookDetails);

        //mock hook retrieval
        when(securityService.withPermission(Permission.REPO_ADMIN, "Get plugin configuration"))
                .thenReturn(escalatedSecurityContextForRepositoryHook);
        when(escalatedSecurityContextForRepositoryHook.call(any(UncheckedOperation.class)))
                .thenReturn(repositoryHook);

        when(securityService.withPermission(Permission.REPO_ADMIN, "Get hook configuration"))
                .thenReturn(escalatedSecurityContextForSettings);
        when(escalatedSecurityContextForSettings.call(any(UncheckedOperation.class)))
                .thenReturn(settings);
    }

    @Test
    public void testOnBranchCreation_errorIfBranchNameDoesNotMatchRegex_repositoryHookConfigured() {
        repositoryHook.setEnabled(true);

        when(repositoryHookDetails.getKey()).thenReturn(anyString());
        when(repositoryHookService.getSettings(repository, anyString())).thenReturn(settings);

        when(event.getBranch()).thenReturn(branch);
        when(branch.getId()).thenReturn("refs/heads/bar");
        when(settings.getString("branchNameRegex")).thenReturn("foo");
        when(i18nService.getKeyedText(anyString(), anyString())).thenReturn(message);

        yaccBranchCreationListener.onBranchCreation(event);

        verify(event, times(1)).cancel(message);
    }

    @Test
    public void testOnBranchCreation_noErrorIfBranchNameDoesNotMatchRegex_repositoryHookConfigured() {
        repositoryHook.setEnabled(true);

        when(repositoryHookDetails.getKey()).thenReturn(anyString());
        when(repositoryHookService.getSettings(repository, anyString())).thenReturn(settings);

        when(event.getBranch()).thenReturn(branch);
        when(branch.getId()).thenReturn("refs/heads/foo");
        when(settings.getString("branchNameRegex")).thenReturn("foo");
        when(i18nService.getKeyedText(anyString(), anyString())).thenReturn(message);

        yaccBranchCreationListener.onBranchCreation(event);

        verify(event, never()).cancel(message);
    }

    @Test
    public void testOnBranchCreation_errorIfBranchNameDoesNotMatchRegex_globalConfigured(){
        when(pluginSettingsFactory.createGlobalSettings()).thenReturn(pluginSettings);
        when(pluginSettings.get(anyString())).thenReturn(settingsMap);
        when(repositoryHookService.createSettingsBuilder()).thenReturn(settingsBuilder);
        when(settingsBuilder.addAll(anyMap())).thenReturn(settingsBuilder);
        when(settingsBuilder.build()).thenReturn(settings);

        when(event.getBranch()).thenReturn(branch);
        when(branch.getId()).thenReturn("refs/heads/bar");
        when(settings.getString("branchNameRegex")).thenReturn("foo");
        when(i18nService.getKeyedText(anyString(), anyString())).thenReturn(message);

        yaccBranchCreationListener.onBranchCreation(event);

        verify(event, times(1)).cancel(message);
    }

    @Test
    public void testOnBranchCreation_noErrorIfBranchNameDoesNotMatchRegex_globalConfigured(){
        when(pluginSettingsFactory.createGlobalSettings()).thenReturn(pluginSettings);
        when(pluginSettings.get(anyString())).thenReturn(settingsMap);
        when(repositoryHookService.createSettingsBuilder()).thenReturn(settingsBuilder);
        when(settingsBuilder.addAll(anyMap())).thenReturn(settingsBuilder);
        when(settingsBuilder.build()).thenReturn(settings);

        when(event.getBranch()).thenReturn(branch);
        when(branch.getId()).thenReturn("refs/heads/foo");
        when(settings.getString("branchNameRegex")).thenReturn("foo");
        when(i18nService.getKeyedText(anyString(), anyString())).thenReturn(message);

        yaccBranchCreationListener.onBranchCreation(event);

        verify(event, never()).cancel(message);
    }
}
