package org.acme.patient.onboarding;

import io.temporal.client.WorkflowOptions;
import org.acme.patient.onboarding.model.Patient;
import org.acme.patient.onboarding.app.Onboarding;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;


@ApplicationScoped
@Path("/onboard")
@Tag(name = "New Patient Onboarding Endpoint")
public class OnboardingResource {

    @Inject
    WorkflowApplicationObserver observer;

    @ConfigProperty(name = "onboarding.task.queue")
    String taskQueue;

    @POST
    public Patient doOnboard(Patient patient) {
        System.out.print("-------Starting Patient Onboarding---------------");
        // start a new workflow execution
        // use the patient id for the unique id
        Onboarding workflow =
                observer.getClient().newWorkflowStub(
                        Onboarding.class, WorkflowOptions.newBuilder()
                                .setWorkflowId(patient.getId())
                                .setTaskQueue(taskQueue).build());

        return workflow.onboardNewPatient(patient);
    }

    @GET
    public String getStatus(@QueryParam("id") String patientId) {
        System.out.print("-------Getting Status for :"+ patientId);
        // query workflow to get the status message
        try {
            Onboarding workflow = observer.getClient().newWorkflowStub(Onboarding.class, patientId);
            return workflow.getStatus();
        } catch (Exception e) {
            e.printStackTrace();
            return "Unable to query workflow with id: " + patientId;
        }
    }
}

