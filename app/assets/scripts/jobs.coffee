a = ['ss', 'pp', 'qq', 'rr', 'ee', 'dd']
@jobs = {}

jobs.Job = (data) ->
  @job_id = m.prop(data.job_id)
  @state = m.prop(data.state)
  @code = m.prop(data.code)
  return # This return statement is important!

jobs.JobList = Array

jobs.vm = do ->
  vm = {}

  vm.init = ->

    vm.list = new (jobs.JobList)

    vm.onclick = (event) ->
      jobID = event()
      $.ajax(
        async : false
        url: '/jobs/' + jobID
        type: 'POST')

    vm.delete = (desc) ->
      alert desc


    vm.update = (desc, state, code) ->
      i = 0
      while i < vm.list.length
        job = vm.list[i]
        if job.job_id() == desc
          vm.list[i] = new (jobs.Job)(
            job_id: desc
            state: state
            code: code)
          return
        i++
      vm.list.push new (jobs.Job)( job_id: desc, state: state, code: code)

    # Send Ajax call to retrieve all Jobs from the Server

    $.post("/jobs", (data) ->
        m.startComputation()
        for job in data.jobs
          vm.update(job.i, job.s, "")
        m.endComputation()
    )

  vm
#the controller defines what part of the model is relevant for the current page
#in our case, there's only one view-model that handles everything

jobs.controller = ->
  jobs.vm.init()

#here's the view

jobs.view = ->
  [ [ jobs.vm.list.map((task) ->
    m 'tr[class=job]',   [
      m('td[class=' + a[task.state()] + ']')
      m('td',  m('a[href="/#/jobs/' + task.job_id() + '"]', task.job_id()))
    ]
  ) ] ]

m.mount(document.getElementById('jobtable-rows'),  { controller: jobs.controller, view: jobs.view})

###
  {onclick: jobs.vm.onclick.bind(task, task.job_id)}
  <ul class="vertical dropdown menu" data-dropdown-menu>
    <li>
        <a href="#">Item 1</a>
        <ul class="menu">
            <li><a href="#">Item 1A</a></li>
                <!-- ... -->
        </ul>
    </li>
</ul>

###