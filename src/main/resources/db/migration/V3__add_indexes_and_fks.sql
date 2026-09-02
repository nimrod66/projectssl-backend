-- Missing indexes for frequently queried columns
create index idx_applicant_email on applicants (email);
create index idx_applicant_status on applicants (status);
create index idx_inter_email on inter_applicants (email);
create index idx_inter_status on inter_applicants (status);
create index idx_contract_status on contracts (status);
create index idx_contract_employer on contracts (employer_id);
create index idx_employer_status on employers (status);
create index idx_notif_recipient_read on notifications (recipient_staff_id, is_read);
create index idx_recruit_app_pair on recruitment_applications (applicant_id, opportunity_id);
create index idx_offer_application_status on offers (application_id, status);
create index idx_task_opportunity on tasks (related_opportunity_id);

-- Missing foreign keys (columns are nullable or tables empty, safe to add)
alter table contract_amendments add constraint fk_amendment_contract foreign key (contract_id) references contracts (id);
alter table payment_schedules add constraint fk_payment_contract foreign key (contract_id) references contracts (id);
alter table notifications add constraint fk_notification_recipient foreign key (recipient_staff_id) references staff (id);
alter table tasks add constraint fk_task_applicant foreign key (related_applicant_id) references applicants (id);
alter table tasks add constraint fk_task_opportunity foreign key (related_opportunity_id) references opportunities (id);
alter table placements add constraint fk_placement_inter foreign key (inter_application_id) references inter_applicants (id);
