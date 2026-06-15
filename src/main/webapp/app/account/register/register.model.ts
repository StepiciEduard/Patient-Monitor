export class Registration {
  constructor(
    public login: string,
    public email: string,
    public password: string,
    public langKey: string,
    public firstName: string = '',
    public lastName: string = '',
    public doctorId: number | null = null,
    public cnp: string = '',
    public dateOfBirth: string = '',
    public gender: string = '',
    public phoneNumber: string = '',
  ) {}
}
