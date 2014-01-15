package liveplugin.toolwindow.addplugin.git;


import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidatorEx;
import com.intellij.openapi.ui.Messages;
import liveplugin.IdeUtil;
import liveplugin.LivePluginAppComponent;
import liveplugin.toolwindow.RefreshPluginTreeAction;
import liveplugin.toolwindow.addplugin.git.jetbrains.plugins.github.api.GithubApiUtil;
import liveplugin.toolwindow.addplugin.git.jetbrains.plugins.github.api.GithubGist;
import liveplugin.toolwindow.addplugin.git.jetbrains.plugins.github.util.GithubAuthData;
import liveplugin.toolwindow.util.PluginsIO;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;

import static liveplugin.LivePluginAppComponent.pluginsRootPath;
import static liveplugin.toolwindow.addplugin.git.jetbrains.plugins.github.GithubIcons.Github_icon;

public class AddPluginFromGistAction extends AnAction {
	private static final Logger log = Logger.getInstance(AddPluginFromGistAction.class);
	private static final Icon defaultIcon = null;

	public AddPluginFromGistAction() {
		super("Plugin from Gist", "Plugin from Gist", Github_icon);
	}

	@Override public void actionPerformed(AnActionEvent event) {
		final Project project = event.getProject();

		String gistUrl = askUserForGistUrl(event);
		if (gistUrl == null) return;

		fetchGistFrom(gistUrl, event, new FetchGistCallback() {
			@Override public void onSuccess(GithubGist gist) {
				String newPluginId = askUserForPluginId(project);
				if (newPluginId == null) return;

				try {
					createPluginFrom(gist, newPluginId);
				} catch (IOException e) {
					showMessageThatCreatingPluginFailed(e, newPluginId, project);
				}

				new RefreshPluginTreeAction().actionPerformed(null);
			}

			@Override public void onFailure(IOException e) {
				showMessageThatFetchingGistFailed(e, project);
			}
		});
	}

	private static String askUserForGistUrl(AnActionEvent event) {
		return Messages.showInputDialog(
				event.getProject(),
				"Enter gist URL:",
				"Add Plugin From Gist",
				defaultIcon, "", new GistUrlValidator());
	}

	private static void fetchGistFrom(final String gistUrl, AnActionEvent event, final FetchGistCallback callback) {
		new Task.Backgroundable(event.getProject(), "Fetching gist", false, PerformInBackgroundOption.ALWAYS_BACKGROUND) {
			private GithubGist gist;
			private IOException exception;

			@Override public void run(@NotNull ProgressIndicator indicator) {
				try {
					gist = GithubApiUtil.getGist(GithubAuthData.createAnonymous(), GistUrlValidator.extractGistIdFrom(gistUrl));
				} catch (IOException e) {
					exception = e;
				}
			}

			@Override public void onSuccess() {
				if (exception != null) {
					callback.onFailure(exception);
				} else {
					callback.onSuccess(gist);
				}
			}
		}.queue();
	}

	private static String askUserForPluginId(Project project) {
		return Messages.showInputDialog(
				project,
				"Enter new plugin name:",
				"New Plugin",
				defaultIcon, "", new PluginIdValidator());
	}

	private static void createPluginFrom(GithubGist gist, String pluginId) throws IOException {
		for (GithubGist.GistFile gistFile : gist.getFiles()) {
			PluginsIO.createFile(pluginsRootPath() + "/" + pluginId, gistFile.getFilename(), gistFile.getContent());
		}
	}

	private void showMessageThatCreatingPluginFailed(IOException e, String newPluginId, Project project) {
		IdeUtil.showErrorDialog(
				project,
				"Error adding plugin \"" + newPluginId + "\" to " + LivePluginAppComponent.pluginsRootPath(),
				"Add Plugin"
		);
		log.info(e);
	}

	private static void showMessageThatFetchingGistFailed(IOException e, Project project) {
		IdeUtil.showErrorDialog(project, "Failed to fetch gist", "LivePlugin");
		log.info(e);
	}

	private interface FetchGistCallback {
		void onSuccess(GithubGist gist);

		void onFailure(IOException e);
	}

	private static class GistUrlValidator implements InputValidatorEx {
		private String errorText;

		@Override public boolean checkInput(String inputString) {
			boolean isValid = inputString.lastIndexOf('/') != -1;
			errorText = isValid ? null : "Gist URL should have at least one '/' symbol";
			return isValid;
		}

		@Nullable @Override public String getErrorText(String inputString) {
			return errorText;
		}

		@Override public boolean canClose(String inputString) {
			return true;
		}

		public static String extractGistIdFrom(String gistUrl) {
			int i = gistUrl.lastIndexOf('/');
			return gistUrl.substring(i + 1);
		}
	}

	private static class PluginIdValidator implements InputValidatorEx {
		private String errorText;

		@Override public boolean checkInput(String pluginId) {
			boolean isValid = !LivePluginAppComponent.pluginExists(pluginId);
			errorText = isValid ? null : "There is already a plugin with this name";
			return isValid;
		}

		@Nullable @Override public String getErrorText(String pluginId) {
			return errorText;
		}

		@Override public boolean canClose(String pluginId) {
			return true;
		}
	}
}