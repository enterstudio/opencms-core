/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/input/form/Attic/CmsForm.java,v $
 * Date   : $Date: 2011/02/14 10:02:24 $
 * Version: $Revision: 1.20 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.gwt.client.ui.input.form;

import org.opencms.gwt.client.ui.css.I_CmsInputCss;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.ui.input.I_CmsFormField;
import org.opencms.gwt.client.ui.input.I_CmsFormWidget;
import org.opencms.gwt.client.ui.input.I_CmsHasBlur;
import org.opencms.gwt.client.validation.CmsValidationController;
import org.opencms.gwt.client.validation.I_CmsValidationHandler;
import org.opencms.gwt.shared.CmsValidationResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.HasKeyPressHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;

/**
 * 
 * This class acts as a container for form fields.<p>
 * 
 * It is also responsible for collecting and validating the values of the form fields.
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.20 $
 * 
 * @since 8.0.0
 * 
 */
public class CmsForm {

    /** The CSS bundle used for this form. **/
    private static final I_CmsInputCss CSS = I_CmsInputLayoutBundle.INSTANCE.inputCss();

    /** The set of fields which have been edited. */
    protected Set<String> m_editedFields = new HashSet<String>();

    /** A map from field ids to the corresponding widgets. */
    protected Map<String, I_CmsFormField> m_fields = new LinkedHashMap<String, I_CmsFormField>();

    /** A reference to the dialog this form is contained in. */
    protected I_CmsFormDialog m_formDialog;

    /** The form handler. */
    protected I_CmsFormHandler m_formHandler;

    /** A flag which indicates whether the user has pressed enter in a widget. */
    protected boolean m_pressedEnter;

    /** The tab for advanced form fields. */
    private FlowPanel m_advancedTab = new FlowPanel();

    /** The tab for basic form fields. */
    private FlowPanel m_basicTab = new FlowPanel();

    /** The fields indexed by model id. */
    private Multimap<String, I_CmsFormField> m_fieldsByModelId = ArrayListMultimap.create();

    /** The initial values of the form fields. */
    private Map<String, String> m_initialValues = new HashMap<String, String>();

    /** 
    private boolean m_isSubmittable;

    /** The list of form reset handlers. */
    private List<I_CmsFormResetHandler> m_resetHandlers = new ArrayList<I_CmsFormResetHandler>();

    /** The server-side form validator class to use. */
    private String m_validatorClass;

    /** The form widget container. */
    private A_CmsFormFieldPanel m_widget;

    /**
     * Creates a new form with an existing form widget container.<p>
     * 
     * @param panel the form widget container 
     */
    public CmsForm(A_CmsFormFieldPanel panel) {

        m_widget = panel;
    }

    /**
     * Creates a new form and optionally sets the form widget container to a simple form field panel.<p>
     * 
     * @param initPanel if true, initializes the form widget container 
     */
    public CmsForm(boolean initPanel) {

        if (initPanel) {
            setWidget(new CmsSimpleFormFieldPanel());
        }
    }

    /**
     * Adds a form field.<p>
     * 
     * @param field the field to add 
     * @param initialValue the initial field value 
     */
    public void addField(I_CmsFormField field, String initialValue) {

        addField("", field, initialValue);
    }

    /**
     * Adds a form field to the form.<p>
     * 
     * @param fieldGroup the form field group key  
     * @param formField the form field which should be added
     */
    public void addField(String fieldGroup, final I_CmsFormField formField) {

        String initialValue = formField.getWidget().getFormValueAsString();
        m_initialValues.put(formField.getId(), initialValue);
        initializeFormFieldWidget(formField);
        m_fields.put(formField.getId(), formField);
        String modelId = formField.getModelId();
        m_fieldsByModelId.put(modelId, formField);
        m_widget.addField(formField, fieldGroup);

    }

    /**
     * Adds a form field to the form and sets its initial value.<p>
     * 
     * @param fieldGroup the form field group key 
     * @param formField the form field which should be added 
     * @param initialValue the initial value of the form field, or null if the field shouldn't have an initial value 
     */
    public void addField(String fieldGroup, I_CmsFormField formField, String initialValue) {

        if (initialValue != null) {
            formField.getWidget().setFormValueAsString(initialValue);
        }
        addField(fieldGroup, formField);
    }

    /**
     * Adds a text label.<p>
     * 
     * @param labelText the text for the label
     * @param advanced if true, add the label to the advanced tab, else to the basic tab 
     * 
     * @return a label with the given text
     */
    public Label addLabel(String labelText, boolean advanced) {

        Label label = new Label(labelText);
        label.setStyleName(CSS.formDescriptionLabel());
        getPanel(advanced).add(label);
        return label;
    }

    /** 
     * Adds a new form reset handler to the form.<p>
     * 
     * @param handler the new form reset handler 
     */
    public void addResetHandler(I_CmsFormResetHandler handler) {

        m_resetHandlers.add(handler);
    }

    /**
     * Adds a separator below the last added form field.<p>
     * 
     * @param advanced if true, adds the separator to the advanced tab, else to the basic tab 
     * 
     */
    public void addSeparator(boolean advanced) {

        getPanel(advanced).add(new CmsSeparator());
    }

    /**
     * Collects all values from the form fields.<p>
     * 
     * This method omits form fields whose values are null.
     * 
     * @return a map of the form field values 
     */
    public Map<String, String> collectValues() {

        Map<String, String> result = new HashMap<String, String>();
        for (Map.Entry<String, I_CmsFormField> entry : m_fields.entrySet()) {
            String key = entry.getKey();
            String value = null;
            I_CmsFormField field = entry.getValue();
            I_CmsFormWidget widget = field.getWidget();
            value = widget.getFormValueAsString();
            result.put(key, value);
        }
        return result;
    }

    /**
     * Performs an initial validation of all form fields.<p>
     */
    public void doInitialValidation() {

        CmsValidationController validationController = new CmsValidationController(
            m_fields.values(),
            createValidationHandler());
        validationController.setFormValidator(m_validatorClass);
        validationController.setFormValidatorConfig(createValidatorConfig());
        startValidation(validationController);
    }

    /**
     * Returns the set of names of fields which have been edited by the user in the current form.<p>
     *  
     * @return the set of names of fields edited by the user 
     */
    public Set<String> getEditedFields() {

        return m_editedFields;

    }

    /**
     * Returns the form field with a given id.<p>
     * 
     * @param id the id of the form field 
     * 
     * @return the form field with the given id, or null if no field was found 
     */
    public I_CmsFormField getField(String id) {

        return m_fields.get(id);
    }

    /** 
     * Returns a map of this form's field, indexed by their field name.<p>
     * 
     * @return a map of form fields 
     */
    public Map<String, I_CmsFormField> getFields() {

        return Collections.unmodifiableMap(m_fields);
    }

    /**
     * Returns the form widget container.<p>
     * 
     * @return the form widget container 
     */
    public A_CmsFormFieldPanel getWidget() {

        return m_widget;
    }

    /**
     * Returns true if none of the fields in a collection are marked as invalid.<p>
     *
     * @param fields the form fields
     * 
     * @return true if none of the fields are invalid 
     */
    public boolean noFieldsInvalid(Collection<I_CmsFormField> fields) {

        for (I_CmsFormField field : fields) {
            if (field.getValidationStatus().equals(I_CmsFormField.ValidationStatus.invalid)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Sets the form dialog in which this form is being used.<p>
     * 
     * @param dialog the form dialog 
     */
    public void setFormDialog(I_CmsFormDialog dialog) {

        m_formDialog = dialog;
    }

    /**
     * Sets the form handler for this form.<p>
     * 
     * @param handler the form handler 
     */
    public void setFormHandler(I_CmsFormHandler handler) {

        m_formHandler = handler;
    }

    /**
     * Sets the server-side form validator class to use.<p>
     * 
     * @param validatorClass the form validator class name 
     */
    public void setValidatorClass(String validatorClass) {

        m_validatorClass = validatorClass;
    }

    /**
     * Sets the form widget container. 
     * 
     * @param widget the form widget container 
     */
    public void setWidget(A_CmsFormFieldPanel widget) {

        assert m_widget == null;
        m_widget = widget;
    }

    /**
     * Validates the form fields and submits their values if the validation was successful.<p>
     */
    public void validateAndSubmit() {

        CmsValidationController validationController = new CmsValidationController(
            m_fields.values(),
            new I_CmsValidationHandler() {

                /**
                 * @see org.opencms.gwt.client.validation.I_CmsValidationHandler#onValidationFinished(boolean)
                 */
                public void onValidationFinished(boolean ok) {

                    if (ok) {
                        m_formDialog.closeDialog();
                        m_formHandler.onSubmitForm(collectValues(), m_editedFields);

                    } else {
                        m_formDialog.setOkButtonEnabled(noFieldsInvalid(m_fields.values()));
                    }
                }

                /**
                 * @see org.opencms.gwt.client.validation.I_CmsValidationHandler#onValidationResult(java.lang.String, org.opencms.gwt.shared.CmsValidationResult)
                 */
                public void onValidationResult(String field, CmsValidationResult result) {

                    updateFieldValidationStatus(field, result);

                }

            });
        validationController.setFormValidator(m_validatorClass);
        validationController.setFormValidatorConfig(createValidatorConfig());
        startValidation(validationController);
    }

    /**
     * Validates a single field.<p>
     * 
     * @param field the field to validate 
     */
    public void validateField(final I_CmsFormField field) {

        CmsValidationController validationController = new CmsValidationController(field, createValidationHandler());
        validationController.setFormValidator(m_validatorClass);
        validationController.setFormValidatorConfig(createValidatorConfig());
        startValidation(validationController);
    }

    /**
     * Returns the configuration string for the server side form validator.<p>
     * 
     * @return the form validator configuration string 
     */
    protected String createValidatorConfig() {

        return "";
    }

    /**
     * Gets the fields with a given model id.<p>
     * 
     * @param modelId the model id 
     * 
     * @return the fields with the given model id 
     */
    protected Collection<I_CmsFormField> getFieldsByModelId(String modelId) {

        return m_fieldsByModelId.get(modelId);
    }

    /**
     * Returns either the basic or advanced tab based on a boolean value.<p>
     *  
     * @param advanced if true, the advanced tab will be returned, else the basic tab
     *  
     * @return the basic or advanced tab 
     */
    protected Panel getPanel(boolean advanced) {

        return advanced ? m_advancedTab : m_basicTab;
    }

    /**
     * Updates the field validation status.<p>
     * 
     * @param field the form field 
     * @param result the validation result 
     */
    protected void updateFieldValidationStatus(I_CmsFormField field, CmsValidationResult result) {

        if (result.hasNewValue()) {
            field.getWidget().setFormValueAsString(result.getNewValue());
        }
        String errorMessage = result.getErrorMessage();
        field.getWidget().setErrorMessage(result.getErrorMessage());
        field.setValidationStatus(errorMessage == null
        ? I_CmsFormField.ValidationStatus.valid
        : I_CmsFormField.ValidationStatus.invalid);
    }

    /**
     * Applies a validation result to a form field.<p>
     * 
     * @param fieldId the field id to which the validation result should be applied 
     * @param result the result of the validation operation 
     */
    protected void updateFieldValidationStatus(String fieldId, CmsValidationResult result) {

        I_CmsFormField field = m_fields.get(fieldId);
        updateFieldValidationStatus(field, result);
    }

    /**
     * Updates the model validation status.<p>
     * 
     * @param modelId the model id
     * @param result the validation result  
     */
    protected void updateModelValidationStatus(String modelId, CmsValidationResult result) {

        Collection<I_CmsFormField> fields = getFieldsByModelId(modelId);
        for (I_CmsFormField field : fields) {
            updateFieldValidationStatus(field, result);
        }

    }

    /**
    * Creates a validation handler which updates the OK button state when validation results come in.<p>
    * 
    * @return a validation handler 
    */
    private I_CmsValidationHandler createValidationHandler() {

        return new I_CmsValidationHandler() {

            /**
             * @see org.opencms.gwt.client.validation.I_CmsValidationHandler#onValidationFinished(boolean)
             */
            public void onValidationFinished(boolean ok) {

                m_formDialog.setOkButtonEnabled(noFieldsInvalid(m_fields.values()));
            }

            /**
             * @see org.opencms.gwt.client.validation.I_CmsValidationHandler#onValidationResult(java.lang.String, org.opencms.gwt.shared.CmsValidationResult)
             */
            public void onValidationResult(String modelId, CmsValidationResult result) {

                updateModelValidationStatus(modelId, result);
            }
        };
    }

    /**
     * Initializes the widget for a new form field.<p>
     * 
     * @param formField the form field whose widget should be initialized 
     */
    @SuppressWarnings("unchecked")
    private void initializeFormFieldWidget(final I_CmsFormField formField) {

        final I_CmsFormWidget widget = formField.getWidget();
        if (widget instanceof HasValueChangeHandlers<?>) {
            ((HasValueChangeHandlers<String>)widget).addValueChangeHandler(new ValueChangeHandler<String>() {

                /**
                 * @see com.google.gwt.event.logical.shared.ValueChangeHandler#onValueChange(ValueChangeEvent event) 
                 */
                public void onValueChange(ValueChangeEvent<String> event) {

                    m_editedFields.add(formField.getId());
                    formField.setValidationStatus(I_CmsFormField.ValidationStatus.unknown);

                    // if the user presses enter, the keypressed event is fired before the change event,
                    // so we use a flag to keep track of whether enter was pressed.
                    if (!m_pressedEnter) {
                        validateField(formField);
                    } else {
                        validateAndSubmit();
                    }
                }
            });
        }

        if (widget instanceof HasKeyPressHandlers) {
            ((HasKeyPressHandlers)widget).addKeyPressHandler(new KeyPressHandler() {

                /**
                 * @see com.google.gwt.event.dom.client.KeyPressHandler#onKeyPress(com.google.gwt.event.dom.client.KeyPressEvent)
                 */
                public void onKeyPress(KeyPressEvent event) {

                    int keyCode = event.getNativeEvent().getKeyCode();
                    if (keyCode == KeyCodes.KEY_ENTER) {
                        m_pressedEnter = true;
                        if (widget instanceof I_CmsHasBlur) {
                            // force a blur because not all browsers send a change event if the user just presses enter in a field
                            ((I_CmsHasBlur)widget).blur();
                        }
                        // make sure that the flag is set to false again after the other events have been processed 
                        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                            /**
                             * @see com.google.gwt.core.client.Scheduler.ScheduledCommand#execute()
                             */
                            public void execute() {

                                m_pressedEnter = false;
                            }
                        });
                    }
                }
            });
        }
    }

    /**
     * Starts the validation of the form.<p>
     * 
     * @param validationController the validation controller to use for the validation 
     */
    private void startValidation(CmsValidationController validationController) {

        validationController.startValidation();
    }

}
