/*
 * Copyright 2020 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.emailtemplatesui.screen.emailtemplate.report;


import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.emailtemplates.entity.ReportEmailTemplate;
import io.jmix.emailtemplates.entity.TemplateReport;
import io.jmix.emailtemplatesui.screen.emailtemplate.AbstractTemplateEditor;
import io.jmix.emailtemplatesui.screen.emailtemplate.parameters.EmailTemplateParametersFragment;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.ui.Fragments;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.EntityComboBox;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.TextField;
import io.jmix.ui.component.VBoxLayout;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.DataContext;
import io.jmix.ui.model.DataLoader;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Named;
import java.util.ArrayList;

@UiController("emailtemplates_ReportEmailTemplate.edit")
@UiDescriptor("report-email-template-edit.xml")
@EditedEntityContainer("emailTemplateDc")
public class ReportEmailTemplateEdit extends AbstractTemplateEditor<ReportEmailTemplate> {

    @Named("defaultGroup.subject")
    private TextField<String> subjectField;

    @Autowired
    private EntityComboBox<Report> emailBody;

    @Autowired
    private Metadata metadata;

    @Autowired
    private Notifications notifications;

    @Autowired
    private MessageBundle messageBundle;

    @Autowired
    protected VBoxLayout defaultValuesBox;

    @Autowired
    protected Fragments fragments;

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected CollectionContainer<Report> emailBodiesDc;

    @Autowired
    protected DataLoader emailBodiesDl;

    protected EmailTemplateParametersFragment parametersFragment;

    @Autowired
    protected InstanceContainer<TemplateReport> emailBodyReportDc;

    @Autowired
    protected DataLoader emailTemplateDl;

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent e) {
        emailBodiesDl.load();
        emailTemplateDl.load();

        parametersFragment = fragments.create(this, EmailTemplateParametersFragment.class)
                .setIsDefaultValues(true)
                .setHideReportCaption(true);
        defaultValuesBox.add(parametersFragment.getFragment());

        parametersFragment.setTemplateReport(getEditedEntity().getEmailBodyReport());

        if (getEditedEntity().getEmailBodyReport() != null) {
            parametersFragment.createComponents();
        } else {
            parametersFragment.clearComponents();
        }

        emailBody.setValue(getEditedEntity().getReport());
        emailBody.addValueChangeListener(reportValueChangeEvent -> updateParametersComponents());

        setSubjectVisibility();
    }

    @Subscribe("useReportSubject")
    protected void useReportSubjectChkBoxValueChange(HasValue.ValueChangeEvent<Boolean> e) {
        setSubjectVisibility();
        if (BooleanUtils.isTrue(e.getValue())) {
            getEditedEntity().setSubject(null);
        }
    }

    private void updateParametersComponents() {
        Report value = emailBody.getValue();
        if (value != null) {
            Report report = dataManager.load(Report.class)
                    .id(value.getId())
                    .fetchPlan("emailTemplate-fetchPlan")
                    .one();
            if (report.getDefaultTemplate() != null) {
                if (ReportOutputType.HTML == report.getDefaultTemplate().getReportOutputType()) {
                    TemplateReport templateReport = metadata.create(TemplateReport.class);
                    templateReport.setParameterValues(new ArrayList<>());
                    templateReport.setReport(report);
                    emailBodyReportDc.setItem(templateReport);

                    getEditedEntity().setEmailBodyReport(templateReport);

                    parametersFragment.setTemplateReport(getEditedEntity().getEmailBodyReport());
                    parametersFragment.createComponents();
                } else {
                    resetTemplateReport();
                    emailBody.setValue(null);
                    notifications.create(Notifications.NotificationType.ERROR)
                            .withDescription(messageBundle.getMessage("notification.reportIsNotHtml"))
                            .show();
                }
            } else {
                resetTemplateReport();
                emailBody.setValue(null);
                notifications.create(Notifications.NotificationType.ERROR)
                        .withDescription(messageBundle.getMessage("notification.reportHasNoDefaultTemplate"))
                        .show();
            }
        } else {
            resetTemplateReport();
        }
    }

    @Subscribe(target = Target.DATA_CONTEXT)
    protected void onPreCommit(DataContext.PreCommitEvent event) {
        TemplateReport templateReport = emailBodyReportDc.getItemOrNull();
        if (templateReport != null) {
            event.getModifiedInstances().add(templateReport);
        }
    }

    protected void resetTemplateReport() {
        emailBodyReportDc.setItem(null);
        parametersFragment.setTemplateReport(getEditedEntity().getEmailBodyReport());
        parametersFragment.clearComponents();
    }

    public void setSubjectVisibility() {
        subjectField.setVisible(BooleanUtils.isNotTrue(getEditedEntity().getUseReportSubject()));
    }

    @Override
    protected boolean preCommit() {
        super.preCommit();
       /* todo: if (!PersistenceHelper.isNew(getEditedEntity())) {
            ReportEmailTemplate original = getDsContext().getDataSupplier().reload(getEditedEntity(), "emailTemplate-fetchPlan");
            ReportEmailTemplate current = getEditedEntity();
            TemplateReport originalEmailBodyReport = original.getEmailBodyReport();
            if (originalEmailBodyReport != null && !originalEmailBodyReport.equals(current.getEmailBodyReport())) {
                entitiesToRemove.addAll(originalEmailBodyReport.getParameterValues());
                entitiesToRemove.add(originalEmailBodyReport);
            }
            if (current.getEmailBodyReport() != null) {
                bodyParameterValuesDc.setModified(true);
            }
        }*/
        return true;
    }

    public void runReport() {
    }
}