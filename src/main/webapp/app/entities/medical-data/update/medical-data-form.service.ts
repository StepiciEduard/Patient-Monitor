import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';
import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { IMedicalData, NewMedicalData } from '../medical-data.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IMedicalData for edit and NewMedicalDataFormGroupInput for create.
 */
type MedicalDataFormGroupInput = IMedicalData | PartialWithRequiredKeyOf<NewMedicalData>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IMedicalData | NewMedicalData> = Omit<T, 'timestamp'> & {
  timestamp?: string | null;
};

type MedicalDataFormRawValue = FormValueOf<IMedicalData>;

type NewMedicalDataFormRawValue = FormValueOf<NewMedicalData>;

type MedicalDataFormDefaults = Pick<NewMedicalData, 'id' | 'timestamp' | 'isAnomaly'>;

type MedicalDataFormGroupContent = {
  id: FormControl<MedicalDataFormRawValue['id'] | NewMedicalData['id']>;
  timestamp: FormControl<MedicalDataFormRawValue['timestamp']>;
  heartRate: FormControl<MedicalDataFormRawValue['heartRate']>;
  spo2: FormControl<MedicalDataFormRawValue['spo2']>;
  temperature: FormControl<MedicalDataFormRawValue['temperature']>;
  systolicBp: FormControl<MedicalDataFormRawValue['systolicBp']>;
  diastolicBp: FormControl<MedicalDataFormRawValue['diastolicBp']>;
  hrv: FormControl<MedicalDataFormRawValue['hrv']>;
  qtInterval: FormControl<MedicalDataFormRawValue['qtInterval']>;
  bnp: FormControl<MedicalDataFormRawValue['bnp']>;
  bloodGlucose: FormControl<MedicalDataFormRawValue['bloodGlucose']>;
  respiratoryRate: FormControl<MedicalDataFormRawValue['respiratoryRate']>;
  fev1: FormControl<MedicalDataFormRawValue['fev1']>;
  etco2: FormControl<MedicalDataFormRawValue['etco2']>;
  anomalyScore: FormControl<MedicalDataFormRawValue['anomalyScore']>;
  isAnomaly: FormControl<MedicalDataFormRawValue['isAnomaly']>;
  patient: FormControl<MedicalDataFormRawValue['patient']>;
};

export type MedicalDataFormGroup = FormGroup<MedicalDataFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class MedicalDataFormService {
  createMedicalDataFormGroup(medicalData: MedicalDataFormGroupInput = { id: null }): MedicalDataFormGroup {
    const medicalDataRawValue = this.convertMedicalDataToMedicalDataRawValue({
      ...this.getFormDefaults(),
      ...medicalData,
    });
    return new FormGroup<MedicalDataFormGroupContent>({
      id: new FormControl(
        { value: medicalDataRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      timestamp: new FormControl(medicalDataRawValue.timestamp, {
        validators: [Validators.required],
      }),
      heartRate: new FormControl(medicalDataRawValue.heartRate),
      spo2: new FormControl(medicalDataRawValue.spo2),
      temperature: new FormControl(medicalDataRawValue.temperature),
      systolicBp: new FormControl(medicalDataRawValue.systolicBp),
      diastolicBp: new FormControl(medicalDataRawValue.diastolicBp),
      hrv: new FormControl(medicalDataRawValue.hrv),
      qtInterval: new FormControl(medicalDataRawValue.qtInterval),
      bnp: new FormControl(medicalDataRawValue.bnp),
      bloodGlucose: new FormControl(medicalDataRawValue.bloodGlucose),
      respiratoryRate: new FormControl(medicalDataRawValue.respiratoryRate),
      fev1: new FormControl(medicalDataRawValue.fev1),
      etco2: new FormControl(medicalDataRawValue.etco2),
      anomalyScore: new FormControl(medicalDataRawValue.anomalyScore),
      isAnomaly: new FormControl(medicalDataRawValue.isAnomaly),
      patient: new FormControl(medicalDataRawValue.patient, {
        validators: [Validators.required],
      }),
    });
  }

  getMedicalData(form: MedicalDataFormGroup): IMedicalData | NewMedicalData {
    return this.convertMedicalDataRawValueToMedicalData(form.getRawValue() as MedicalDataFormRawValue | NewMedicalDataFormRawValue);
  }

  resetForm(form: MedicalDataFormGroup, medicalData: MedicalDataFormGroupInput): void {
    const medicalDataRawValue = this.convertMedicalDataToMedicalDataRawValue({ ...this.getFormDefaults(), ...medicalData });
    form.reset(
      {
        ...medicalDataRawValue,
        id: { value: medicalDataRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): MedicalDataFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      timestamp: currentTime,
      isAnomaly: false,
    };
  }

  private convertMedicalDataRawValueToMedicalData(
    rawMedicalData: MedicalDataFormRawValue | NewMedicalDataFormRawValue,
  ): IMedicalData | NewMedicalData {
    return {
      ...rawMedicalData,
      timestamp: dayjs(rawMedicalData.timestamp, DATE_TIME_FORMAT),
    };
  }

  private convertMedicalDataToMedicalDataRawValue(
    medicalData: IMedicalData | (Partial<NewMedicalData> & MedicalDataFormDefaults),
  ): MedicalDataFormRawValue | PartialWithRequiredKeyOf<NewMedicalDataFormRawValue> {
    return {
      ...medicalData,
      timestamp: medicalData.timestamp ? medicalData.timestamp.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
