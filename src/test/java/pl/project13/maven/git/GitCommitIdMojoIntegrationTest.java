/*
 * This file is part of git-commit-id-plugin by Konrad Malawski <konrad.malawski@java.pl>
 *
 * git-commit-id-plugin is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * git-commit-id-plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with git-commit-id-plugin.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.project13.maven.git;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.Test;
import pl.project13.maven.git.FileSystemMavenSandbox.CleanUp;

import java.io.File;
import java.util.Properties;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.internal.util.reflection.Whitebox.setInternalState;

public class GitCommitIdMojoIntegrationTest extends GitIntegrationTest {

  @Test
  public void shouldResolvePropertiesOnDefaultSettingsForNonPomProject() throws Exception {
    mavenSandbox.withParentProject("my-jar-project", "jar").withNoChildProject().withGitRepoInParent(AvailableGitTestRepo.WITH_ONE_COMMIT).create(CleanUp.CLEANUP_FIRST);
    MavenProject targetProject = mavenSandbox.getParentProject();
    setProjectToExecuteMojoIn(targetProject);

    // when
    mojo.execute();

    // then
    assertGitPropertiesPresentInProject(targetProject.getProperties());
  }

  @Test
  public void shouldNotRunWhenPackagingPomAndDefaultSettingsApply() throws Exception {
    // given
    mavenSandbox.withParentProject("my-pom-project", "pom").withNoChildProject().withGitRepoInParent(AvailableGitTestRepo.WITH_ONE_COMMIT).create(CleanUp.CLEANUP_FIRST);
    MavenProject targetProject = mavenSandbox.getParentProject();
    setProjectToExecuteMojoIn(targetProject);

    // when
    mojo.execute();

    // then
    assertThat(targetProject.getProperties()).isEmpty();
  }

  @Test
  public void shouldRunWhenPackagingPomAndSkipPomsFalse() throws Exception {
    // given
    mavenSandbox.withParentProject("my-pom-project", "pom").withNoChildProject().withGitRepoInParent(AvailableGitTestRepo.WITH_ONE_COMMIT).create(CleanUp.CLEANUP_FIRST);
    MavenProject targetProject = mavenSandbox.getParentProject();
    setProjectToExecuteMojoIn(targetProject);
    alterMojoSettings("skipPoms", false);

    // when
    mojo.execute();

    // then
    assertThat(targetProject.getProperties()).isNotEmpty();
  }

  @Test
  public void shouldUseParentProjectRepoWhenInvokedFromChild() throws Exception {
    // given
    mavenSandbox.withParentProject("my-pom-project", "pom").withChildProject("my-jar-module", "jar").withGitRepoInParent(AvailableGitTestRepo.WITH_ONE_COMMIT).create(CleanUp.CLEANUP_FIRST);
    MavenProject targetProject = mavenSandbox.getChildProject();
    setProjectToExecuteMojoIn(targetProject);
    alterMojoSettings("skipPoms", false);

    // when
    mojo.execute();

    // then
    assertGitPropertiesPresentInProject(targetProject.getProperties());
  }

  @Test
  public void shouldUseChildProjectRepoIfInvokedFromChild() throws Exception {
    // given
    mavenSandbox.withParentProject("my-pom-project", "pom").withChildProject("my-jar-module", "jar").withGitRepoInChild(AvailableGitTestRepo.WITH_ONE_COMMIT).create(CleanUp.CLEANUP_FIRST);
    MavenProject targetProject = mavenSandbox.getChildProject();
    setProjectToExecuteMojoIn(targetProject);
    alterMojoSettings("skipPoms", false);

    // when
    mojo.execute();

    // then
    assertGitPropertiesPresentInProject(targetProject.getProperties());
  }

  @Test(expected = MojoExecutionException.class)
  public void shouldFailWithExceptionWhenNoGitRepoFound() throws Exception {
    // given
    mavenSandbox.withParentProject("my-pom-project", "pom").withChildProject("my-jar-module", "jar").withNoGitRepoAvailable().create(CleanUp.CLEANUP_FIRST);
    MavenProject targetProject = mavenSandbox.getChildProject();
    setProjectToExecuteMojoIn(targetProject);
    alterMojoSettings("skipPoms", false);

    // when
    mojo.execute();
  }

  private void alterMojoSettings(String parameterName, Object parameterValue) {
    setInternalState(mojo, parameterName, parameterValue);
  }

  private void setProjectToExecuteMojoIn(MavenProject project) {
    setInternalState(mojo, "project", project);
    setInternalState(mojo, "dotGitDirectory", new File(project.getBasedir(), ".git"));
  }

  private void assertGitPropertiesPresentInProject(Properties properties) {
    assertThat(properties).satisfies(new ContainsKeyCondition("git.branch"));
    assertThat(properties).satisfies(new ContainsKeyCondition("git.commit.id"));
    assertThat(properties).satisfies(new ContainsKeyCondition("git.commit.id.abbrev"));
    assertThat(properties).satisfies(new ContainsKeyCondition("git.build.user.name"));
    assertThat(properties).satisfies(new ContainsKeyCondition("git.build.user.email"));
    assertThat(properties).satisfies(new ContainsKeyCondition("git.commit.user.name"));
    assertThat(properties).satisfies(new ContainsKeyCondition("git.commit.user.email"));
    assertThat(properties).satisfies(new ContainsKeyCondition("git.commit.message.full"));
    assertThat(properties).satisfies(new ContainsKeyCondition("git.commit.message.short"));
    assertThat(properties).satisfies(new ContainsKeyCondition("git.commit.time"));
  }

}