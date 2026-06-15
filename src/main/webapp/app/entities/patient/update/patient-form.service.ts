import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { IPatient, NewPatient } from '../patient.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IPatient for edit and NewPatientFormGroupInput for create.
 */
type PatientFormGroupInput = IPatient | PartialWithRequiredKeyOf<NewPatient>;

type PatientFormDefaults = Pick<NewPatient, 'id'>;

type PatientFormGroupContent = {
  id: FormControl<IPatient['id'] | NewPatient['id']>;
  cnp: FormControl<IPatient['cnp']>;
  phoneNumber: FormControl<IPatient['phoneNumber']>;
  address: FormControl<IPatient['address']>;
  patientType: FormControl<IPatient['patientType']>;
  patientSubtype: FormControl<IPatient['patientSubtype']>;
  dateOfBirth: FormControl<IPatient['dateOfBirth']>;
  gender: FormControl<IPatient['gender']>;
  hba1c: FormControl<IPatient['hba1c']>;
  bmi: FormControl<IPatient['bmi']>;
  fev1Baseline: FormControl<IPatient['fev1Baseline']>;
  user: FormControl<IPatient['user']>;
  doctor: FormControl<IPatient['doctor']>;
};

export type PatientFormGroup = FormGroup<PatientFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class PatientFormService {
  createPatientFormGroup(patient: PatientFormGroupInput = { id: null }): PatientFormGroup {
    const patientRawValue = {
      ...this.getFormDefaults(),
      ...patient,
    };
    return new FormGroup<PatientFormGroupContent>({
      id: new FormControl(
        { value: patientRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      cnp: new FormControl(patientRawValue.cnp, {
        validators: [Validators.required, Validators.minLength(13), Validators.maxLength(13)],
      }),
      phoneNumber: new FormControl(patientRawValue.phoneNumber, {
        validators: [Validators.maxLength(20)],
      }),
      address: new FormControl(patientRawValue.address, {
        validators: [Validators.maxLength(500)],
      }),
      patientType: new FormControl(patientRawValue.patientType, {
        validators: [Validators.required],
      }),
      patientSubtype: new FormControl(patientRawValue.patientSubtype, {
        validators: [Validators.required],
      }),
      dateOfBirth: new FormControl(patientRawValue.dateOfBirth, {
        validators: [Validators.required],
      }),
      gender: new FormControl(patientRawValue.gender, {
        validators: [Validators.maxLength(10)],
      }),
      hba1c: new FormControl(patientRawValue.hba1c),
      bmi: new FormControl(patientRawValue.bmi),
      fev1Baseline: new FormControl(patientRawValue.fev1Baseline),
      user: new FormControl(patientRawValue.user, {
        validators: [Validators.required],
      }),
      doctor: new FormControl(patientRawValue.doctor, {
        validators: [Validators.required],
      }),
    });
  }

  getPatient(form: PatientFormGroup): IPatient | NewPatient {
    return form.getRawValue() as IPatient | NewPatient;
  }

  resetForm(form: PatientFormGroup, patient: PatientFormGroupInput): void {
    const patientRawValue = { ...this.getFormDefaults(), ...patient };
    form.reset(
      {
        ...patientRawValue,
        id: { value: patientRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): PatientFormDefaults {
    return {
      id: null,
    };
  }
}
