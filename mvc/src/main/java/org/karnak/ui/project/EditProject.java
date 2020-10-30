package org.karnak.ui.project;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import org.karnak.data.gateway.Destination;
import org.karnak.data.gateway.Project;
import org.karnak.ui.component.ConfirmDialog;
import org.karnak.ui.data.ProjectDataProvider;
import org.karnak.ui.forwardnode.ProfileDropDown;

import java.util.List;

public class EditProject extends VerticalLayout {
    private ProjectDataProvider projectDataProvider;
    private Binder<Project> binder;
    private TextField textProjectName;
    private ProjectSecret projectSecret;
    private ProfileDropDown profileDropDown;
    private HorizontalLayout horizontalLayoutButtons;
    private Button buttonUpdate;
    private Button buttonRemove;
    private WarningRemoveProjectUsed dialogWarning;
    private Project project;

    public EditProject(ProjectDataProvider projectDataProvider) {
        this.projectDataProvider = projectDataProvider;
        dialogWarning = new WarningRemoveProjectUsed();

        setEnabled(false);
        setElements();
        setEventButtonAdd();
        setEventButtonRemove();
        add(textProjectName, profileDropDown, projectSecret, horizontalLayoutButtons);
    }

    public void setProject(Project project) {
        if (project != null) {
            this.project = projectDataProvider.getProjectById(project.getId());
            binder.setBean(this.project);
            setEnabled(true);
        } else {
            this.project = null;
            binder.removeBean();
            clear();
            setEnabled(false);
        }
    }

    private void setEventButtonAdd() {
        buttonUpdate.addClickListener(event -> {
            if (project != null && binder.writeBeanIfValid(project)){
                if (project.getDestinations()!=null && project.getDestinations().size()>0) {
                    ConfirmDialog dialog = new ConfirmDialog(
                    String.format("The project %s is used, are you sure you want to updated ?", project.getName()));
                    dialog.addConfirmationListener(componentEvent -> {
                        projectDataProvider.update(project);
                    });
                    dialog.open();
                } else {
                    projectDataProvider.update(project);
                }
            }
        });
    }

    private void setEventButtonRemove() {
        buttonRemove.addClickListener(e -> {
            List<Destination> destinations = project.getDestinations();
            if (destinations != null && destinations.size() > 0) {
                dialogWarning.setText(project);
                dialogWarning.open();

            } else {
                projectDataProvider.remove(project);
                clear();
                setEnabled(false);
            }
        });
    }

    private void setElements() {
        TextFieldsBindProject textFieldsBindProject = new TextFieldsBindProject();
        binder = textFieldsBindProject.getBinder();
        textProjectName = textFieldsBindProject.getTextResearchName();
        profileDropDown = textFieldsBindProject.getProfileDropDown();
        projectSecret = new ProjectSecret(textFieldsBindProject.getTextSecret());

        textProjectName.setLabel("Project Name");
        textProjectName.setWidthFull();

        profileDropDown.setLabel("De-identification Profile");
        profileDropDown.setWidthFull();

        buttonUpdate = new Button("Update");
        buttonRemove = new Button("Remove");
        buttonRemove.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
        horizontalLayoutButtons = new HorizontalLayout(buttonUpdate, buttonRemove);
    }

    private void clear() {
        binder.readBean(new Project());
    }
}
