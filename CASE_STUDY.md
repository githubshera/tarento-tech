# Case Study -- Warehouse and Store Cost Management

This document outlines a business-oriented approach to cost management
for the warehouse and store management system. The focus is on business
value, key considerations, and the information that should be gathered
before defining detailed technical requirements.

------------------------------------------------------------------------

## 1. Cost allocation and tracking across Warehouses and Stores

**Question:** How would you approach allocating and tracking costs
across different Warehouses and Stores? What challenges and
considerations would you take into account, and what information would
you need before defining a solution?

**Answer:**

I would start by defining a consistent cost model so that costs can be
recorded, categorized, and attributed to the correct Warehouse, Store,
and business activity. The objective would be to provide management with
a reliable view of where costs are incurred and how efficiently each
location operates.

I would group costs into the following categories:

-   **Labour costs:** Salaries, overtime, temporary staff, and other
    workforce-related expenses.
-   **Inventory costs:** Purchasing, handling, storage, damage,
    shrinkage, and write-offs.
-   **Transportation costs:** Inbound deliveries, transfers between
    Warehouses, and deliveries to Stores.
-   **Overhead costs:** Rent, utilities, maintenance, insurance,
    equipment, and shared operational expenses.

Direct costs should be assigned to the Warehouse or Store that incurred
them. Shared costs, such as central administration or shared
transportation, would need an agreed allocation method. Depending on the
business activity, allocation could be based on storage area, inventory
volume, number of orders, working hours, or shipment count.

Some key challenges and considerations would be:

1.  **Consistent cost definitions:** Finance and operations teams must
    agree on what each cost category includes.
2.  **Shared costs:** A fair and transparent allocation method is needed
    when one cost benefits multiple locations.
3.  **Data quality:** Costs must be linked to valid Warehouse and Store
    identifiers, and missing or duplicated records must be controlled.
4.  **Timing:** Operational events and financial postings may occur at
    different times, so reporting periods and cut-off rules must be
    defined.
5.  **Traceability:** Users should be able to understand how a cost was
    calculated and allocated.
6.  **Historical accuracy:** Changes to location relationships or
    Warehouse replacements should not rewrite historical cost records.

Before defining the solution, I would ask the business and finance
teams:

-   Which cost categories are currently tracked?
-   Which costs are direct and which are shared?
-   What allocation rules are currently used?
-   How frequently should costs be updated and reported?
-   Which financial and operational systems are the sources of truth?
-   What level of detail is required: transaction, daily, monthly,
    Warehouse, Store, or product?
-   Who owns and approves cost allocation rules?

I would first agree on the cost model and ownership of the data with
Finance and Operations. Only then would I define the application changes
and reporting requirements.

------------------------------------------------------------------------

## 2. Cost optimization without compromising service quality

**Question:** How would you identify and implement opportunities to
reduce costs while maintaining service quality? How would you prioritize
the opportunities, and what outcomes would you expect?

**Answer:**

I would approach cost optimization using operational and financial data
together. The objective should not simply be to reduce spending, but to
reduce avoidable costs while maintaining availability, delivery
performance, and customer satisfaction.

I would follow these steps:

### Step 1: Establish a baseline

First, I would collect current cost and service metrics for each
Warehouse and Store. Examples include:

-   Cost per order or shipment.
-   Storage cost per unit.
-   Warehouse capacity utilization.
-   Inventory turnover and ageing.
-   Transportation cost per delivery.
-   Picking and handling time.
-   Stock-out rate and order fulfilment time.
-   Damage, returns, and inventory write-offs.

This baseline would help identify locations or activities with unusually
high costs.

### Step 2: Identify improvement opportunities

Potential opportunities could include:

1.  **Improve warehouse utilization:** Better use of available capacity
    can reduce the need for additional storage space.
2.  **Optimize inventory levels:** Better demand forecasting and
    replenishment can reduce excess stock, storage costs, and
    obsolescence while protecting product availability.
3.  **Optimize transportation:** Consolidating shipments and improving
    delivery routes can reduce transport costs without unnecessarily
    increasing delivery times.
4.  **Improve warehouse processes:** Reducing unnecessary movement,
    manual work, and repeated handling can improve productivity.
5.  **Review supplier and service contracts:** Comparing service levels,
    pricing, and contract terms may reveal opportunities for savings.

### Step 3: Prioritize opportunities

I would prioritize each opportunity based on:

-   Expected financial benefit.
-   Implementation cost and effort.
-   Operational risk.
-   Customer and service-level impact.
-   Time required to achieve the benefit.

High-impact, low-risk opportunities should generally be considered
first. Changes that could affect stock availability or delivery
commitments should be evaluated carefully.

### Step 4: Pilot, measure, and improve

I would test selected changes in one Warehouse or a limited group of
Stores before wider implementation. I would compare the results against
the baseline and agreed service-level targets.

Expected outcomes include lower storage and transportation costs, better
capacity utilization, less excess inventory, improved operational
productivity, and more informed decisions. Any cost reduction should be
measured alongside service metrics to ensure that savings are not
achieved by reducing service quality.

------------------------------------------------------------------------

## 3. Integration with financial systems

**Question:** What are the benefits of integrating the Warehouse and
Store management system with financial systems, and how would you
support seamless synchronization and reporting?

**Answer:**

Integrating the operational system with financial systems would connect
Warehouse and Store activities with the organization's financial
records. It would reduce manual reconciliation and help Finance and
Operations work from consistent information.

The main benefits would include:

1.  **More accurate financial records:** Operational events such as
    inventory receipts, transfers, adjustments, and write-offs can
    provide input to the relevant financial processes.
2.  **Reduced manual work:** Automated data exchange can reduce
    spreadsheet-based processing and repeated data entry.
3.  **Improved visibility:** Management can compare operational activity
    with actual costs and budgets.
4.  **Faster reporting:** Timely data can support more frequent cost and
    performance reporting.
5.  **Better auditability:** Each financial entry should be traceable to
    its originating business event and reference.

For reliable integration, I would first agree on the ownership of each
data element and the events that need to be exchanged. The systems
should use consistent identifiers for Warehouses, Stores, products, and
financial accounts.

I would also consider the following design requirements:

-   **Defined interfaces and data contracts:** Agree on message formats,
    mandatory fields, validation rules, and versioning.
-   **Reliable delivery:** Use retry handling for temporary failures and
    ensure that processing is idempotent so that duplicate messages do
    not create duplicate financial entries.
-   **Error handling and reconciliation:** Failed or rejected records
    should be visible to support teams and should be possible to
    reconcile.
-   **Timeliness:** Determine which information needs near-real-time
    processing and which can be transferred in scheduled batches.
-   **Security and audit:** Apply appropriate access controls and retain
    an audit trail of financial changes.
-   **Monitoring:** Track integration health, processing delays,
    failures, and reconciliation differences.

I would not assume that every operational event must immediately create
a financial posting. Finance should define the accounting rules, posting
frequency, approval requirements, and treatment of corrections before
implementation.

------------------------------------------------------------------------

## 4. Budgeting and forecasting

**Question:** How would you use the system to support future cost
planning and resource allocation? What factors would you consider when
designing budgeting and forecasting capabilities?

**Answer:**

I would use historical operational and financial data as the starting
point for budgeting and forecasting. The system should help the business
understand expected costs, compare actual spending against budgets, and
identify potential issues early.

The forecasting model could consider:

-   Historical costs by Warehouse and Store.
-   Sales and demand forecasts.
-   Expected inventory levels and product mix.
-   Seasonal patterns and promotional activity.
-   Planned changes in Warehouse capacity or Store locations.
-   Labour requirements, wage changes, and overtime.
-   Supplier pricing and transportation rates.
-   Rent, utilities, maintenance, and other overhead changes.
-   Business growth plans and expected service levels.

I would support different planning periods, such as monthly, quarterly,
and annual budgets. Users should be able to compare budget, forecast,
and actual costs and review variances by cost category and location.

Important design considerations include:

1.  **Clear ownership:** Finance should own budget rules and approval
    processes, while operational teams provide forecasts and
    assumptions.
2.  **Consistent dimensions:** Costs should be reportable by period,
    location, cost category, and other agreed business dimensions.
3.  **Scenario planning:** Users should be able to compare scenarios
    such as higher demand, increased transport rates, or a new
    Warehouse.
4.  **Versioning and approvals:** Approved budgets and revised forecasts
    should be retained so changes can be tracked.
5.  **Data quality and timeliness:** Forecasts are only useful when
    source data is accurate and updated at an appropriate frequency.
6.  **Explainability:** Users should be able to see the assumptions
    behind a forecast rather than receiving only a final number.

I would begin with a clear set of business requirements and a small
number of useful reports, then expand the forecasting capabilities based
on feedback and data maturity.

------------------------------------------------------------------------

## 5. Cost control during Warehouse replacement

**Question:** How would you ensure cost control when replacing a
Warehouse, particularly when the existing Business Unit Code is reused
and historical information must be preserved?

**Answer:**

I would treat Warehouse replacement as a new operational record linked
to the history of the previous Warehouse, rather than overwriting or
deleting the existing record.

This is important because the Business Unit Code may be reused, but the
old and new Warehouses represent different periods of operation and may
have different capacities, locations, and cost profiles.

My approach would include the following:

1.  **Preserve historical records:** Archive the existing Warehouse and
    retain its original identifier, creation date, archive date, and
    associated historical information.
2.  **Create a new Warehouse record:** Create the replacement with a new
    database identifier while reusing the Business Unit Code, as
    required by the business rule.
3.  **Preserve stock correctly:** Validate that the replacement has
    enough capacity for the existing stock and that the stock quantity
    is transferred consistently.
4.  **Maintain cost history:** Keep costs incurred by the old Warehouse
    associated with the old record. New costs should be associated with
    the replacement record from its effective start date.
5.  **Control the replacement budget:** Estimate one-time costs such as
    relocation, transportation, installation, equipment, and temporary
    storage, as well as ongoing operating costs.
6.  **Require approvals where appropriate:** Finance and Operations
    should approve the replacement budget and any material variance.
7.  **Monitor the outcome:** Compare actual replacement costs with the
    approved budget and review service performance during the
    transition.

The replacement process should be transactional for the database changes
so that archiving the old Warehouse and creating the new one happen
together. External financial or operational systems should be updated
through a reliable integration process, with traceable events and
reconciliation.

Before implementation, I would clarify whether stock movement, asset
transfer, depreciation, contract changes, and one-time replacement
expenses need to be represented in this application or remain
responsibilities of external systems.

The key principle is to preserve historical accuracy while giving the
new Warehouse its own operational and financial identity. Reusing a
Business Unit Code should not mean losing the history of the previous
Warehouse or mixing its costs with the replacement's costs.

------------------------------------------------------------------------

## Conclusion

My overall approach would be to establish clear cost definitions and
ownership with Finance and Operations, connect operational events to
reliable financial data, and provide reporting that supports decisions
at Warehouse and Store level. I would prioritize improvements based on
measurable savings and service impact, preserve historical records
during Warehouse replacement, and introduce budgeting and forecasting
capabilities incrementally as the business requirements become clear.
