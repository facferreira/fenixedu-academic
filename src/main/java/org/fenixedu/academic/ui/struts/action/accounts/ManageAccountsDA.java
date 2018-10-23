/**
 * Copyright © 2002 Instituto Superior Técnico
 *
 * This file is part of FenixEdu Academic.
 *
 * FenixEdu Academic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu Academic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu Academic.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fenixedu.academic.ui.struts.action.accounts;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.contacts.EmailAddress;
import org.fenixedu.academic.domain.util.email.Message;
import org.fenixedu.academic.domain.util.email.SystemSender;
import org.fenixedu.academic.dto.person.PersonBean;
import org.fenixedu.academic.ui.struts.action.base.FenixDispatchAction;
import org.fenixedu.academic.util.Bundle;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.domain.exceptions.DomainException;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.portal.domain.PortalConfiguration;
import org.fenixedu.bennu.struts.annotations.Forward;
import org.fenixedu.bennu.struts.annotations.Mapping;
import org.fenixedu.bennu.struts.portal.EntryPoint;
import org.fenixedu.bennu.struts.portal.StrutsFunctionality;
import org.fenixedu.commons.i18n.LocalizedString;

import com.google.common.base.Strings;

import pt.ist.fenixWebFramework.renderers.utils.RenderUtils;
import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;
import pt.ist.fenixframework.FenixFramework;

@StrutsFunctionality(app = AccountManagementApp.class, path = "manage-accounts",
        titleKey = "link.accountmanagement.manageaccounts")
@Mapping(path = "/accounts/manageAccounts")
@Forward(name = "manageAccounts", path = "/accounts/manageAccounts.jsp")
@Forward(name = "createPerson", path = "/accounts/createPerson.jsp")
@Forward(name = "createPersonFillInfo", path = "/accounts/createPersonFillInfo.jsp")
@Forward(name = "viewPerson", path = "/accounts/viewPerson.jsp")
public class ManageAccountsDA extends FenixDispatchAction {

    @EntryPoint
    public ActionForward manageAccounts(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) {
        request.setAttribute("searchParameters", new SearchParametersBean());
        return mapping.findForward("manageAccounts");
    }

    public ActionForward search(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) {
        SearchParametersBean parameters = getRenderedObject("searchParameters");
        request.setAttribute("matches", parameters.search());
        return mapping.findForward("manageAccounts");
    }

    public ActionForward prepareCreatePerson(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        request.setAttribute("personBean", new PersonBean());
        return mapping.findForward("createPerson");
    }

    public ActionForward showExistentPersonsWithSameMandatoryDetails(final ActionMapping mapping, final ActionForm actionForm,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        PersonBean bean = getRenderedObject("personBean");
        Collection<Person> results = Collections.emptySet();
        if (!Strings.isNullOrEmpty(bean.getDocumentIdNumber())) {
            results = Person.findPersonByDocumentID(bean.getDocumentIdNumber());
        }
        if (!Strings.isNullOrEmpty(bean.getGivenNames()) || !Strings.isNullOrEmpty(bean.getFamilyNames())) {
            String name = Stream.of(bean.getGivenNames(), bean.getFamilyNames()).filter(n -> !Strings.isNullOrEmpty(n))
                    .collect(Collectors.joining(" "));
            Stream<Person> stream = Person.findPersonStream(name, Integer.MAX_VALUE);
            if (results.isEmpty()) {
                results = stream.collect(Collectors.toSet());
            } else {
                results.addAll(stream.collect(Collectors.toSet()));
            }
        }
        request.setAttribute("resultPersons", results);
        request.setAttribute("createPerson", bean);
        return mapping.findForward("createPerson");
    }

    public ActionForward prepareCreatePersonFillInformation(final ActionMapping mapping, final ActionForm actionForm,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        request.setAttribute("personBean", getRenderedObject("personBean"));
        return mapping.findForward("createPersonFillInfo");
    }

    public ActionForward createNewPerson(final ActionMapping mapping, final ActionForm actionForm,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final PersonBean bean = getRenderedObject();
        try {
            Person person = createAccount(bean);
            SearchParametersBean searchParametersBean = new SearchParametersBean();
            searchParametersBean.setUsername(person.getUsername());
            request.setAttribute("searchParameters", searchParametersBean);
            request.setAttribute("matches", searchParametersBean.search());
            return mapping.findForward("manageAccounts");
        } catch (DomainException e) {
            addActionMessage(request, e.getMessage(), e.getArgs());
            request.setAttribute("personBean", bean);
            return mapping.findForward("createPersonFillInfo");
        }
    }

    @Atomic(mode = TxMode.WRITE)
    private Person createAccount(final PersonBean bean) {
        final Person person = new Person(bean);
        if (person.getUser() == null) {
            person.setUser(new User(person.getProfile()));
        }
        person.getAllPendingPartyContacts().forEach(partyContact -> partyContact.setValid());
        return person;
    }

    public ActionForward invalid(final ActionMapping mapping, final ActionForm actionForm, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        PersonBean bean = getRenderedObject();
        request.setAttribute("personBean", bean);

        return mapping.findForward("createPersonFillInfo");
    }

    public ActionForward createNewPersonPostback(final ActionMapping mapping, final ActionForm actionForm,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        PersonBean bean = getRenderedObject();
        request.setAttribute("personBean", bean);

        RenderUtils.invalidateViewState();

        return mapping.findForward("createPersonFillInfo");
    }

    public ActionForward viewPerson(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final Person person = getDomainObject(request, "personId");
        return viewPerson(person, mapping, request);
    }

    public ActionForward viewPerson(final Person person, final ActionMapping mapping, final HttpServletRequest request)
            throws Exception {
        final PersonBean personBean = new PersonBean(person);

        request.setAttribute("editPersonalInfo", false);
        request.setAttribute("person", person);
        request.setAttribute("personBean", personBean);

        return mapping.findForward("viewPerson");
    }

    public ActionForward prepareEditPersonalData(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response) {
        final Person person = getDomainObject(request, "personId");
        final PersonBean personBean = new PersonBean(person);

        request.setAttribute("editPersonalInfo", true);
        request.setAttribute("person", person);
        request.setAttribute("personBean", personBean);

        return mapping.findForward("viewPerson");
    }

    public ActionForward editPersonalDataInvalid(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final Person person = getDomainObject(request, "personId");
        final PersonBean personBean = getRenderedObject("personBean");

        request.setAttribute("editPersonalInfo", true);
        request.setAttribute("person", person);
        request.setAttribute("personBean", personBean);

        return mapping.findForward("viewPerson");
    }

    public ActionForward editPersonalDataPostback(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final Person person = getDomainObject(request, "personId");
        final PersonBean personBean = getRenderedObject("personBean");

        request.setAttribute("editPersonalInfo", true);
        request.setAttribute("person", person);
        request.setAttribute("personBean", personBean);

        RenderUtils.invalidateViewState();

        return mapping.findForward("viewPerson");
    }

    public ActionForward editPersonalData(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final Person person = getDomainObject(request, "personId");
        final PersonBean personBean = getRenderedObject("personBean");

        try {
            personBean.save();

            return viewPerson(person, mapping, request);
        } catch (DomainException e) {
            addActionMessage(request, e.getMessage(), e.getArgs());

            return editPersonalDataInvalid(mapping, form, request, response);
        }
    }

    public ActionForward deletePerson(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final Person person = getDomainObject(request, "personId");

        try {
            FenixFramework.atomic(() -> {
                User user = person.getUser();
                String to = getSendingEmailAddress(person);

                if (StringUtils.isNotBlank(to)) {
                    sendLastEmail(to);
                }

                person.delete();
                if (user != null) {
                    user.delete();
                }
            });
            return manageAccounts(mapping, form, request, response);
        } catch (DomainException e) {
            addActionMessage("error", request, e.getLocalizedMessage());

            return manageAccounts(mapping, form, request, response);
        }
    }

    private String getSendingEmailAddress(Person person) {
        EmailAddress emailAddress = person.getEmailAddressForSendingEmails();
        String to = "";
        if (emailAddress != null) {
            to = emailAddress.getValue();
        }

        return to;
    }

    private void sendLastEmail(final String to) {
        SystemSender systemSender = Bennu.getInstance().getSystemSender();
        LocalizedString institutionName = Bennu.getInstance().getInstitutionUnit().getNameI18n();
        String supportEmail = PortalConfiguration.getInstance().getSupportEmailAddress();

        StringBuilder sb = new StringBuilder();
        sb.append(BundleUtil.getString(Bundle.STUDENT, new Locale("pt"), "label.account.ManageAccountsDA.email.subject"));
        sb.append("//");
        sb.append(BundleUtil.getString(Bundle.STUDENT, new Locale("en"), "label.account.ManageAccountsDA.email.subject"));
        String subject = sb.toString();

        sb = new StringBuilder();
        sb.append(BundleUtil.getString(Bundle.STUDENT, new Locale("pt"), "label.account.ManageAccountsDA.email.body",
                institutionName.getContent(new Locale("pt")) + " da Universidade de Lisboa", supportEmail));
        sb.append("\n=================================\n");
        sb.append(BundleUtil.getString(Bundle.STUDENT, new Locale("en"), "label.account.ManageAccountsDA.email.body",
                institutionName.getContent(new Locale("en")) + " of the Universidade de Lisboa", supportEmail));
        String body = sb.toString();

        new Message(systemSender, to, subject, body);
    }

}
